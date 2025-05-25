package MDS.Diagnostic_Pacient;

import MDS.Users.User;
import MDS.Users.User_Repository;
import MDS.Diagnostic.Diagnostic;
import MDS.Diagnostic.Diagnostic_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class Diagnostic_Pacient_Controller {

    @Autowired
    private Diagnostic_Pacient_Repository diagnosticPacientRepository;

    @Autowired
    private Diagnostic_Repository diagnosticRepository;

    @Autowired
    private User_Repository userRepository;

    // Obține toți pacienții (rol = "pacient")
    @GetMapping
    public List<User> getAllPatients() {
        return userRepository.findAll()
                .stream()
                .filter(user -> "pacient".equals(user.getRol()))
                .collect(Collectors.toList());
    }

    // Adaugă un diagnostic pentru un pacient
    @PostMapping("/{pacientId}/diagnoses")
    public ResponseEntity<Diagnostic_Pacient> addDiagnostic(
            @PathVariable Long pacientId,
            @RequestBody Map<String, Object> request) {
        Long diagnosticId = Long.valueOf(request.get("diagnosticId").toString());
        Long doctorId = request.containsKey("doctorId") ? Long.valueOf(request.get("doctorId").toString()) : null;

        // Verifică pacientul
        User pacient = userRepository.findById(pacientId)
                .orElseThrow(() -> new RuntimeException("Pacientul nu a fost găsit"));
        if (!"pacient".equals(pacient.getRol())) {
            throw new RuntimeException("ID-ul specificat nu aparține unui pacient!");
        }

        // Verifică diagnosticul
        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new RuntimeException("Diagnosticul nu a fost găsit"));

        // Verifică doctorul (dacă este specificat)
        User doctor = null;
        if (doctorId != null) {
            doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctorul nu a fost găsit"));
            if (!"doctor".equals(doctor.getRol())) {
                throw new RuntimeException("ID-ul specificat nu aparține unui doctor!");
            }
        }

        Diagnostic_Pacient diagnosticPacient = new Diagnostic_Pacient();
        diagnosticPacient.setPacient(pacient); // ID_Pacient va fi USER_ID al pacientului (rol = "pacient")
        diagnosticPacient.setDiagnostic(diagnostic);
        diagnosticPacient.setDataDiagnostic(new Timestamp(System.currentTimeMillis()));
        diagnosticPacient.setDoctor(doctor); // ID_Doctor va fi USER_ID al doctorului (rol = "doctor"), dacă există

        Diagnostic_Pacient saved = diagnosticPacientRepository.save(diagnosticPacient);
        return ResponseEntity.ok(saved);
    }

    // Obține toate diagnosticele unui pacient
    @GetMapping("/{pacientId}/diagnoses")
    public List<Diagnostic_Pacient> getPatientDiagnoses(@PathVariable Long pacientId) {
        User pacient = userRepository.findById(pacientId)
                .orElseThrow(() -> new RuntimeException("Pacientul nu a fost găsit"));
        if (!"pacient".equals(pacient.getRol())) {
            throw new RuntimeException("ID-ul specificat nu aparține unui pacient!");
        }
        return diagnosticPacientRepository.findByPacientUserId(pacientId);
    }
    
}