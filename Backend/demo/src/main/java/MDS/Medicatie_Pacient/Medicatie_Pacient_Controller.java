package MDS.Medicatie_Pacient;

import MDS.Users.User;
import MDS.Users.User_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicatie-pacient")
public class Medicatie_Pacient_Controller {

    @Autowired
    private Medicatie_Pacient_Repository medicatiePacientRepository;

    @Autowired
    private User_Repository userRepository;

    // Get all medication assignments
    @GetMapping
    public List<Medicatie_Pacient> getAllMedicatiePacient() {
        return medicatiePacientRepository.findAll();
    }

    // Get medication assignments for a specific patient
    @GetMapping("/pacient/{pacientId}")
    public ResponseEntity<List<Medicatie_Pacient>> getMedicatieByPacient(@PathVariable Long pacientId) {
        return userRepository.findById(pacientId)
                .filter(user -> "pacient".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(medicatiePacientRepository.findByPacientUserId(pacientId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Get medication assignments prescribed by a specific doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Medicatie_Pacient>> getMedicatieByDoctor(@PathVariable Long doctorId) {
        return userRepository.findById(doctorId)
                .filter(user -> "doctor".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(medicatiePacientRepository.findByDoctorUserId(doctorId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Add a new medication assignment (only doctors can do this)
    @PostMapping
    public ResponseEntity<Medicatie_Pacient> createMedicatiePacient(@RequestBody Medicatie_Pacient medicatiePacient) {
        if (medicatiePacient.getPacient() == null || !"pacient".equals(medicatiePacient.getPacient().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        if (medicatiePacient.getDoctor() != null && !"doctor".equals(medicatiePacient.getDoctor().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        Medicatie_Pacient savedMedicatie = medicatiePacientRepository.save(medicatiePacient);
        return ResponseEntity.ok(savedMedicatie);
    }

    // Update a medication assignment
    @PutMapping("/update/{id}")
    public ResponseEntity<Medicatie_Pacient> updateMedicatiePacient(@PathVariable Long id, @RequestBody Medicatie_Pacient medicatieDetails) {
        return medicatiePacientRepository.findById(id)
                .map(medicatie -> {
                    if (!"pacient".equals(medicatieDetails.getPacient().getRol())) {
                        throw new IllegalArgumentException("Invalid patient role");
                    }
                    if (medicatieDetails.getDoctor() != null && !"doctor".equals(medicatieDetails.getDoctor().getRol())) {
                        throw new IllegalArgumentException("Invalid doctor role");
                    }
                    medicatie.setPacient(medicatieDetails.getPacient());
                    medicatie.setPrescriptie(medicatieDetails.getPrescriptie());
                    medicatie.setDataPrescriere(medicatieDetails.getDataPrescriere());
                    medicatie.setDoza(medicatieDetails.getDoza());
                    medicatie.setFrecventa(medicatieDetails.getFrecventa());
                    medicatie.setDoctor(medicatieDetails.getDoctor());
                    Medicatie_Pacient updatedMedicatie = medicatiePacientRepository.save(medicatie);
                    return ResponseEntity.ok(updatedMedicatie);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a medication assignment
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedicatiePacient(@PathVariable Long id) {
        return medicatiePacientRepository.findById(id)
                .map(medicatie -> {
                    medicatiePacientRepository.delete(medicatie);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}