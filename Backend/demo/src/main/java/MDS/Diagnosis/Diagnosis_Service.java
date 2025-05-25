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

}