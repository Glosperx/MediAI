package MDS.Diagnosis;

import MDS.Consultatie.Consultatie;
import MDS.Consultatie.Consultatie_Repository;
import MDS.Diagnostic.Diagnostic;
import MDS.Diagnostic.Diagnostic_Repository;
import MDS.Diagnostic_Pacient.Diagnostic_Pacient;
import MDS.Diagnostic_Pacient.Diagnostic_Pacient_Repository;
import MDS.Medicatie.Medicatie;
import MDS.Medicatie.Medicatie_Repository;
import MDS.Medicatie_Pacient.Medicatie_Pacient;
import MDS.Medicatie_Pacient.Medicatie_Pacient_Repository;
import MDS.Simptome.Simptom;
import MDS.Simptome.Simptom_Repository;
import MDS.Simptome_Pacient.Simptom_Pacient;
import MDS.Simptome_Pacient.Simptom_Pacient_Repository;
import MDS.Users.User;
import MDS.Users.User_Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class Diagnosis_Service {
    private static final String FLASK_API_URL = "http://localhost:5000/predict";
    private static final Logger logger = LoggerFactory.getLogger(Diagnosis_Service.class);

    @Autowired
    private Diagnostic_Pacient_Repository diagnosticPacientRepository;

    @Autowired
    private Medicatie_Pacient_Repository medicatiePacientRepository;

    @Autowired
    private Simptom_Pacient_Repository simptomPacientRepository;

    @Autowired
    private Consultatie_Repository consultatieRepository;

    @Autowired
    private Diagnostic_Repository diagnosticRepository;

    @Autowired
    private Medicatie_Repository medicatieRepository;

    @Autowired
    private Simptom_Repository simptomRepository;

    @Autowired
    private User_Repository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Diagnosis_Response getDiagnosis(Diagnosis_Request request) {
        try {
            logger.info("Sending request to Flask API: {}", request);
            ResponseEntity<Map> response = restTemplate.postForEntity(FLASK_API_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                logger.info("Received Flask API response: {}", responseBody);

                Diagnosis_Response diagnosisResponse = new Diagnosis_Response();
                diagnosisResponse.setDiagnosis((String) responseBody.get("diagnosis"));
                diagnosisResponse.setMedication((String) responseBody.get("medication"));
                diagnosisResponse.setProbabilities((Map<String, Double>) responseBody.get("probabilities"));
                diagnosisResponse.setIdentifiedSymptoms((List<String>) responseBody.get("identified_symptoms"));
                diagnosisResponse.setNegatedSymptoms((List<String>) responseBody.get("negated_symptoms"));

                logger.info("Diagnosis_Response: diagnosis={}, medication={}, probabilities={}, identifiedSymptoms={}",
                        diagnosisResponse.getDiagnosis(),
                        diagnosisResponse.getMedication(),
                        diagnosisResponse.getProbabilities(),
                        diagnosisResponse.getIdentifiedSymptoms());

                return diagnosisResponse;
            } else {
                logger.error("Invalid Flask API response: status={}", response.getStatusCode());
                throw new RuntimeException("Invalid response from Flask API");
            }
        } catch (Exception e) {
            logger.error("Error communicating with Flask API: {}", e.getMessage(), e);
            throw new RuntimeException("Error communicating with Flask API: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void saveDiagnosisToDatabase(Diagnosis_Response diagnosisResponse, Long pacientId) {
        logger.info("Entering saveDiagnosisToDatabase for patient ID: {}", pacientId);
        try {
            logger.info("Saving diagnosis to database for patient ID: {}", pacientId);

            // Găsim pacientul
            User pacient = userRepository.findById(pacientId)
                    .orElseThrow(() -> new RuntimeException("Pacient not found with ID: " + pacientId));

            // Găsim primul doctor disponibil
            User doctor = userRepository.findFirstByRol("DOCTOR")
                    .orElseThrow(() -> new RuntimeException("No doctor available in the system"));


            Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());

            // 1. Creăm și salvăm consultația mai întâi
            Consultatie consultatie = new Consultatie();
            consultatie.setPacient(pacient);
            consultatie.setDoctor(doctor); // Setăm doctorul ÎNAINTE de a salva
            consultatie.setDataConsultatie(currentTime);
            consultatie.setAprobat(false);

            // Salvăm consultația
            Consultatie savedConsultatie = consultatieRepository.save(consultatie);
            logger.info("Created consultation with ID: {} for patient: {}", savedConsultatie.getId(), pacient.getEmail());


            logger.info("Created new consultation with ID: {} for patient: {} and doctor: {}",
                    consultatie.getId(), pacient.getAcronim(), doctor.getAcronim());

            // 2. Salvăm diagnosticele cu probabilitățile lor
            if (diagnosisResponse.getProbabilities() != null && !diagnosisResponse.getProbabilities().isEmpty()) {
                for (Map.Entry<String, Double> entry : diagnosisResponse.getProbabilities().entrySet()) {
                    final String diagnosticName = entry.getKey();
                    Double probabilitate = entry.getValue();

                    // Căutăm sau creăm diagnosticul
                    Diagnostic diagnostic = diagnosticRepository.findByNume(diagnosticName)
                            .orElseGet(() -> {
                                Diagnostic newDiagnostic = new Diagnostic();
                                newDiagnostic.setNume(diagnosticName);
                                return diagnosticRepository.save(newDiagnostic);
                            });

                    // Salvăm legătura pacient-diagnostic
                    Diagnostic_Pacient diagnosticPacient = new Diagnostic_Pacient();
                    diagnosticPacient.setPacient(pacient);
                    diagnosticPacient.setDiagnostic(diagnostic);
                    diagnosticPacient.setProbabilitate(probabilitate);
                    diagnosticPacient.setDataDiagnostic(currentTime);
                    diagnosticPacientRepository.save(diagnosticPacient);

                    logger.info("Saved diagnostic {} with probability {}", diagnosticName, probabilitate);
                }
            }

            // 3. Salvăm simptomele identificate
            if (diagnosisResponse.getIdentifiedSymptoms() != null) {
                for (String symptomName : diagnosisResponse.getIdentifiedSymptoms()) {
                    // Căutăm sau creăm simptomul
                    Simptom simptom = simptomRepository.findByNume(symptomName)
                            .orElseGet(() -> {
                                Simptom newSimptom = new Simptom();
                                newSimptom.setNume(symptomName);
                                return simptomRepository.save(newSimptom);
                            });

                    // Salvăm legătura pacient-simptom
                    Simptom_Pacient simptomPacient = new Simptom_Pacient();
                    simptomPacient.setPacient(pacient);
                    simptomPacient.setSimptom(simptom);
                    simptomPacient.setDataRaportare(currentTime);
                    simptomPacientRepository.save(simptomPacient);

                    logger.info("Saved identified symptom: {}", symptomName);
                }
            }

            // 4. Salvăm medicația recomandată
            if (diagnosisResponse.getMedication() != null && !diagnosisResponse.getMedication().trim().isEmpty()) {
                String[] medicatii = diagnosisResponse.getMedication().split(",");

                for (String medicationName : medicatii) {
                    String medicatieName = medicationName.trim();

                    if (!medicatieName.isEmpty()) {
                        // Căutăm sau creăm medicația
                        Medicatie medicatie = medicatieRepository.findByNume(medicatieName)
                                .orElseGet(() -> {
                                    Medicatie newMedicatie = new Medicatie();
                                    newMedicatie.setNume(medicatieName);
                                    return medicatieRepository.save(newMedicatie);
                                });

                        // Salvăm legătura pacient-medicație
                        Medicatie_Pacient medicatiePacient = new Medicatie_Pacient();
                        medicatiePacient.setPacient(pacient);
                        medicatiePacient.setPrescriptie(medicatie);
                        medicatiePacient.setDataPrescriere(currentTime);
                        medicatiePacientRepository.save(medicatiePacient);

                        logger.info("Saved medication: {}", medicatieName);
                    }
                }
            }

            logger.info("Successfully saved all diagnosis data to database");

        } catch (Exception e) {
            logger.error("Error saving diagnosis to database: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving diagnosis to database: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Diagnosis_Response getDiagnosisAndSave(Diagnosis_Request request, Long pacientId) {
        // Obținem diagnosticul de la AI
        Diagnosis_Response response = getDiagnosis(request);

        // Îl salvăm în baza de date
        saveDiagnosisToDatabase(response, pacientId);

        return response;
    }
}