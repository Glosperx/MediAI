package MDS.Simptome_Pacient;

import MDS.Users.User;
import MDS.Users.User_Repository;
import MDS.Simptome.Simptom;
import MDS.Simptome.Simptom_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class Simptom_Pacient_Controller {

    @Autowired
    private Simptom_Pacient_Repository simptomPacientRepository;

    @Autowired
    private Simptom_Repository simptomRepository;

    @Autowired
    private User_Repository userRepository;

    // Raportează un simptom pentru un pacient
    @PostMapping("/{pacientId}/symptoms")
    public ResponseEntity<Simptom_Pacient> reportSymptom(
            @PathVariable Long pacientId,
            @RequestBody Map<String, Long> request) {
        Long simptomId = request.get("simptomId");

        User pacient = userRepository.findById(pacientId)
                .orElseThrow(() -> new RuntimeException("Pacientul nu a fost găsit"));
        Simptom simptom = simptomRepository.findById(simptomId)
                .orElseThrow(() -> new RuntimeException("Simptomul nu a fost găsit"));

        Simptom_Pacient simptomPacient = new Simptom_Pacient();
        simptomPacient.setPacient(pacient);
        simptomPacient.setSimptom(simptom);
        simptomPacient.setDataRaportare(new Timestamp(System.currentTimeMillis()));

        Simptom_Pacient saved = simptomPacientRepository.save(simptomPacient);
        return ResponseEntity.ok(saved);
    }

    // Obține toate simptomele raportate ale unui pacient
    @GetMapping("/{pacientId}/symptoms")
    public List<Simptom_Pacient> getPatientSymptoms(@PathVariable Long pacientId) {
        return simptomPacientRepository.findByPacientUserId(pacientId);
    }
}