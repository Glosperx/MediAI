package MDS.Diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/diagnosis")
public class Diagnosis_Controller {

    private static final Logger logger = LoggerFactory.getLogger(Diagnosis_Controller.class);

    @Autowired
    private Diagnosis_Service diagnosisService;

    @Autowired
    private Diagnosis_Email_Service diagnosisEmailService;

    @PostMapping
    public Diagnosis_Response predictDiagnosis(
            @RequestBody Diagnosis_Request request,
            @RequestParam Long pacientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String email) {

        try {
            logger.info("Received diagnosis request for patient ID: {}", pacientId);

            // Obtine diagnosticul si il salveaza in vaza de date
            Diagnosis_Response response = diagnosisService.getDiagnosisAndSave(request, pacientId);

            // Trimite email daca e specificat
            if (email != null && !email.isEmpty()) {
                diagnosisEmailService.sendDiagnosisEmail(email, response);
                logger.info("Diagnosis email sent to: {}", email);
            }

            return response;

        } catch (Exception e) {
            logger.error("Error processing diagnosis request: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing diagnosis: " + e.getMessage());
        }
    }
}