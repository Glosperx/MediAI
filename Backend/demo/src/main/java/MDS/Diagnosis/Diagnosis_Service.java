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

/**
 * Serviciu principal pentru gestionarea diagnosticelor medicale
 * Comunica cu API-ul de AI (Flask) si salveaza rezultatele in baza de date
 */
@Service
public class Diagnosis_Service {

    // URL-ul catre API-ul Flask care ruleaza modelul de AI pentru diagnostice
    private static final String FLASK_API_URL = "http://localhost:5000/predict";
    private static final Logger logger = LoggerFactory.getLogger(Diagnosis_Service.class);

    // Repository-uri pentru toate entitatile din baza de date
    @Autowired
    private Diagnostic_Pacient_Repository diagnosticPacientRepository; // Legaturi diagnostic-pacient

    @Autowired
    private Medicatie_Pacient_Repository medicatiePacientRepository; // Legaturi medicatie-pacient

    @Autowired
    private Simptom_Pacient_Repository simptomPacientRepository; // Legaturi simptom-pacient

    @Autowired
    private Consultatie_Repository consultatieRepository; // Consultatiile medicale

    @Autowired
    private Diagnostic_Repository diagnosticRepository; // Diagnosticele disponibile

    @Autowired
    private Medicatie_Repository medicatieRepository; // Medicatiile disponibile

    @Autowired
    private Simptom_Repository simptomRepository; // Simptomele disponibile

    @Autowired
    private User_Repository userRepository; // Utilizatorii (pacienti si doctori)

    @Autowired
    private RestTemplate restTemplate; // Client HTTP pentru apeluri REST

    /**
     * Obtine un diagnostic de la API-ul de AI (Flask)
     * Trimite simptomele pacientului si primeste diagnostic, medicatie si probabilitati
     */
    public Diagnosis_Response getDiagnosis(Diagnosis_Request request) {
        try {
            logger.info("Sending request to Flask API: {}", request);

            // Apelul HTTP POST catre API-ul Flask
            ResponseEntity<Map> response = restTemplate.postForEntity(FLASK_API_URL, request, Map.class);

            // Verificarea daca raspunsul este valid
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                logger.info("Received Flask API response: {}", responseBody);

                // Construirea obiectului de raspuns din datele primite
                Diagnosis_Response diagnosisResponse = new Diagnosis_Response();
                diagnosisResponse.setDiagnosis((String) responseBody.get("diagnosis")); // Diagnosticul principal
                diagnosisResponse.setMedication((String) responseBody.get("medication")); // Medicatia recomandata
                diagnosisResponse.setProbabilities((Map<String, Double>) responseBody.get("probabilities")); // Probabilitatile pentru fiecare diagnostic
                diagnosisResponse.setIdentifiedSymptoms((List<String>) responseBody.get("identified_symptoms")); // Simptomele identificate
                diagnosisResponse.setNegatedSymptoms((List<String>) responseBody.get("negated_symptoms")); // Simptomele negate/absente

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

    /**
     * Salveaza diagnosticul in baza de date
     * Creeaza intrari in toate tabelele relevante: consultatii, diagnostice, simptome, medicatii
     */
    @Transactional // Toate operatiunile se fac intr-o singura tranzactie
    public void saveDiagnosisToDatabase(Diagnosis_Response diagnosisResponse, Long pacientId) {
        logger.info("Entering saveDiagnosisToDatabase for patient ID: {}", pacientId);
        try {
            logger.info("Saving diagnosis to database for patient ID: {}", pacientId);

            // 1. GASIREA PACIENTULUI
            User pacient = userRepository.findById(pacientId)
                    .orElseThrow(() -> new RuntimeException("Pacient not found with ID: " + pacientId));

            // 2. GASIREA UNUI DOCTOR (primul disponibil)
            User doctor = userRepository.findFirstByRol("DOCTOR")
                    .orElseThrow(() -> new RuntimeException("No doctor available in the system"));

            Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());

            // 3. CREAREA CONSULTATIEI
            // Fiecare diagnostic trebuie asociat cu o consultatie
            Consultatie consultatie = new Consultatie();
            consultatie.setPacient(pacient);
            consultatie.setDoctor(doctor); // Asocierea cu un doctor
            consultatie.setDataConsultatie(currentTime);
            consultatie.setAprobat(false); // Consultatie neaprobata initial (va fi verificata de doctor)

            // Salvarea consultatiei in baza de date
            Consultatie savedConsultatie = consultatieRepository.save(consultatie);
            logger.info("Created consultation with ID: {} for patient: {}", savedConsultatie.getId(), pacient.getEmail());

            logger.info("Created new consultation with ID: {} for patient: {} and doctor: {}",
                    consultatie.getId(), pacient.getAcronim(), doctor.getAcronim());

            // 4. SALVAREA DIAGNOSTICELOR CU PROBABILITATILE
            // AI-ul returneaza multiple diagnostice posibile cu probabilitati
            if (diagnosisResponse.getProbabilities() != null && !diagnosisResponse.getProbabilities().isEmpty()) {
                for (Map.Entry<String, Double> entry : diagnosisResponse.getProbabilities().entrySet()) {
                    final String diagnosticName = entry.getKey(); // Numele diagnosticului
                    Double probabilitate = entry.getValue(); // Probabilitatea (0.0 - 1.0)

                    // Cautam diagnosticul in baza de date sau il cream daca nu exista
                    Diagnostic diagnostic = diagnosticRepository.findByNume(diagnosticName)
                            .orElseGet(() -> {
                                Diagnostic newDiagnostic = new Diagnostic();
                                newDiagnostic.setNume(diagnosticName);
                                return diagnosticRepository.save(newDiagnostic);
                            });

                    // Cream legatura intre pacient si diagnostic cu probabilitatea corespunzatoare
                    Diagnostic_Pacient diagnosticPacient = new Diagnostic_Pacient();
                    diagnosticPacient.setPacient(pacient);
                    diagnosticPacient.setDiagnostic(diagnostic);
                    diagnosticPacient.setProbabilitate(probabilitate);
                    diagnosticPacient.setDataDiagnostic(currentTime);
                    diagnosticPacientRepository.save(diagnosticPacient);

                    logger.info("Saved diagnostic {} with probability {}", diagnosticName, probabilitate);
                }
            }

            // 5. SALVAREA SIMPTOMELOR IDENTIFICATE
            // AI-ul identifica care simptome sunt prezente la pacient
            if (diagnosisResponse.getIdentifiedSymptoms() != null) {
                for (String symptomName : diagnosisResponse.getIdentifiedSymptoms()) {
                    // Cautam simptomul in baza de date sau il cream daca nu exista
                    Simptom simptom = simptomRepository.findByNume(symptomName)
                            .orElseGet(() -> {
                                Simptom newSimptom = new Simptom();
                                newSimptom.setNume(symptomName);
                                return simptomRepository.save(newSimptom);
                            });

                    // Cream legatura intre pacient si simptom
                    Simptom_Pacient simptomPacient = new Simptom_Pacient();
                    simptomPacient.setPacient(pacient);
                    simptomPacient.setSimptom(simptom);
                    simptomPacient.setDataRaportare(currentTime);
                    simptomPacientRepository.save(simptomPacient);

                    logger.info("Saved identified symptom: {}", symptomName);
                }
            }

            // 6. SALVAREA MEDICATIEI RECOMANDATE
            // AI-ul recomanda medicatie pentru diagnostic
            if (diagnosisResponse.getMedication() != null && !diagnosisResponse.getMedication().trim().isEmpty()) {
                // Medicatia poate fi o lista separata prin virgule
                String[] medicatii = diagnosisResponse.getMedication().split(",");

                for (String medicationName : medicatii) {
                    String medicatieName = medicationName.trim();

                    if (!medicatieName.isEmpty()) {
                        // Cautam medicatia in baza de date sau o cream daca nu exista
                        Medicatie medicatie = medicatieRepository.findByNume(medicatieName)
                                .orElseGet(() -> {
                                    Medicatie newMedicatie = new Medicatie();
                                    newMedicatie.setNume(medicatieName);
                                    return medicatieRepository.save(newMedicatie);
                                });

                        // Cream legatura intre pacient si medicatie (prescriptie)
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

    /**
     * Metoda combinata: obtine diagnosticul de la AI si il salveaza in baza de date
     * Aceasta este metoda principala apelata din controller
     */
    @Transactional
    public Diagnosis_Response getDiagnosisAndSave(Diagnosis_Request request, Long pacientId) {
        // Pasul 1: Obtinem diagnosticul de la AI-ul Flask
        Diagnosis_Response response = getDiagnosis(request);

        // Pasul 2: Salvam rezultatul in baza de date
        saveDiagnosisToDatabase(response, pacientId);

        // Pasul 3: Returnam raspunsul pentru afisare in UI
        return response;
    }
}