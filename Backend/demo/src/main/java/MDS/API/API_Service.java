// Clasa de serviciu pentru comunicarea cu API-ul medical
// Foloseste RestTemplate pentru cereri HTTP

package MDS.API;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class API_Service {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    // Constructor cu URL-ul API-ului
    public API_Service(@Value("${medical.ai.api.url:http://localhost:5000}") String apiBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.apiBaseUrl = apiBaseUrl;
    }

    // Verifica starea API-ului
    public boolean checkApiHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(apiBaseUrl + "/api/health", Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtine lista de simptome
    public List<String> getAvailableSymptoms() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(apiBaseUrl + "/api/symptoms", Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<String> symptoms = (List<String>) response.getBody().get("symptoms");
                return symptoms != null ? symptoms : Collections.emptyList();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Trimite cerere de predictie cu simptome
    public PredictionResult getPrediction(List<String> symptoms) {
        try {
            // Creeaza body-ul cererii
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("symptoms", symptoms);

            // Seteaza header-ele
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Creeaza entitatea HTTP
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Trimite cererea POST
            ResponseEntity<Map> response = restTemplate.postForEntity(apiBaseUrl + "/api/predict", entity, Map.class);

            // Proceseaza raspunsul
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                PredictionResult result = new PredictionResult();
                result.setPrimaryDiagnosis((String) responseBody.get("prediction"));

                List<Map<String, Object>> topDiseasesMap = (List<Map<String, Object>>) responseBody.get("top_diseases");
                List<DiseaseConfidence> topDiseases = new ArrayList<>();
                for (Map<String, Object> disease : topDiseasesMap) {
                    DiseaseConfidence dc = new DiseaseConfidence();
                    dc.setDisease((String) disease.get("disease"));
                    dc.setConfidence(((Number) disease.get("confidence")).doubleValue());
                    topDiseases.add(dc);
                }

                result.setTopDiseases(topDiseases);
                return result;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Clasa pentru rezultatul predictiei
    public static class PredictionResult {
        private String primaryDiagnosis;
        private List<DiseaseConfidence> topDiseases;

        public String getPrimaryDiagnosis() {
            return primaryDiagnosis;
        }

        public void setPrimaryDiagnosis(String primaryDiagnosis) {
            this.primaryDiagnosis = primaryDiagnosis;
        }

        public List<DiseaseConfidence> getTopDiseases() {
            return topDiseases;
        }

        public void setTopDiseases(List<DiseaseConfidence> topDiseases) {
            this.topDiseases = topDiseases;
        }
    }

    // Clasa pentru boala si probabilitatea ei
    public static class DiseaseConfidence {
        private String disease;
        private double confidence;

        public String getDisease() {
            return disease;
        }

        public void setDisease(String disease) {
            this.disease = disease;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
}