// Controller pentru gestionarea diagnosticelor pacientilor
// Ofera endpoint-uri pentru operatii legate de pacienti si diagnosticele lor

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

    // Obtine toti pacientii (rol = "pacient")
    @GetMapping
    public List<User> getAllPatients() {
        return userRepository.findAll()
                .stream()
                .filter(user -> "pacient".equals(user.getRol()))
                .collect(Collectors.toList());
    }

    // Adauga un diagnostic pentru un pacient
    @PostMapping("/{pacientId}/diagnoses")
    public ResponseEntity<Diagnostic_Pacient> addDiagnostic(
            @PathVariable Long pacientId,
            @RequestBody Map<String, Object> request) {
        Long diagnosticId = Long.valueOf(request.get("diagnosticId").toString());
        Long doctorId = request.containsKey("doctorId") ? Long.valueOf(request.get("doctorId").toString()) : null;

        // Verifica pacientul
        User pacient = userRepository.findById(pacientId)
                .orElseThrow(() -> new RuntimeException("Pacientul nu a fost gasit"));
        if (!"pacient".equals(pacient.getRol())) {
            throw new RuntimeException("ID-ul specificat nu apartine unui pacient!");
        }

        // Verifica diagnosticul
        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new RuntimeException("Diagnosticul nu a fost gasit"));

        // Verifica doctorul (daca este specificat)
        User doctor = null;
        if (doctorId != null) {
            doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctorul nu a fost gasit"));
            if (!"doctor".equals(doctor.getRol())) {
                throw new RuntimeException("ID-ul specificat nu apartine unui doctor!");
            }
        }

        Diagnostic_Pacient diagnosticPacient = new Diagnostic_Pacient();
        diagnosticPacient.setPacient(pacient); // ID_Pacient este USER_ID al pacientului
        diagnosticPacient.setDiagnostic(diagnostic);
        diagnosticPacient.setDataDiagnostic(new Timestamp(System.currentTimeMillis()));
        diagnosticPacient.setDoctor(doctor); // ID_Doctor este USER_ID al doctorului, daca exista

        Diagnostic_Pacient saved = diagnosticPacientRepository.save(diagnosticPacient);
        return ResponseEntity.ok(saved);
    }

    // Obtine toate diagnosticele unui pacient
    @GetMapping("/{pacientId}/diagnoses")
    public List<Diagnostic_Pacient> getPatientDiagnoses(@PathVariable Long pacientId) {
        User pacient = userRepository.findById(pacientId)
                .orElseThrow(() -> new RuntimeException("Pacientul nu a fost gasit"));
        if (!"pacient".equals(pacient.getRol())) {
            throw new RuntimeException("ID-ul specificat nu apartine unui pacient!");
        }
        return diagnosticPacientRepository.findByPacientUserId(pacientId);
    }
}