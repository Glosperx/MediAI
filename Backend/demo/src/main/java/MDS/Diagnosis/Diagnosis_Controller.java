package MDS.Diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnosis")
public class Diagnosis_Controller {
    @Autowired
    private Diagnosis_Service diagnosisService;

    @PostMapping
    public Diagnosis_Response predictDiagnosis(@RequestBody Diagnosis_Request request) {
        return diagnosisService.getDiagnosis(request);
    }

}