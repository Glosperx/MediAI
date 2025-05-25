package MDS.Consultatie;

import MDS.Users.User;
import MDS.Users.User_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultatie")
public class Consultatie_Controller {

    @Autowired
    private Consultatie_Repository consultatieRepository;

    @Autowired
    private User_Repository userRepository;

    // Get all consultations
    @GetMapping
    public List<Consultatie> getAllConsultatii() {
        return consultatieRepository.findAll();
    }

    // Get consultations for a specific patient
    @GetMapping("/pacient/{pacientId}")
    public ResponseEntity<List<Consultatie>> getConsultatiiByPacient(@PathVariable Long pacientId) {
        return userRepository.findById(pacientId)
                .filter(user -> "pacient".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(consultatieRepository.findByPacientUserId(pacientId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Get consultations for a specific doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Consultatie>> getConsultatiiByDoctor(@PathVariable Long doctorId) {
        return userRepository.findById(doctorId)
                .filter(user -> "doctor".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(consultatieRepository.findByDoctorUserId(doctorId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Add a new consultation
    @PostMapping
    public ResponseEntity<Consultatie> createConsultatie(@RequestBody Consultatie consultatie) {
        if (consultatie.getPacient() == null || !"pacient".equals(consultatie.getPacient().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        if (consultatie.getDoctor() == null || !"doctor".equals(consultatie.getDoctor().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        if (consultatie.getAprobat() != true && consultatie.getAprobat() != false) {
            return ResponseEntity.badRequest().build();
        }
        Consultatie savedConsultatie = consultatieRepository.save(consultatie);
        return ResponseEntity.ok(savedConsultatie);
    }

    // Update a consultation
    @PutMapping("/update/{id}")
    public ResponseEntity<Consultatie> updateConsultatie(@PathVariable Long id, @RequestBody Consultatie consultatieDetails) {
        return consultatieRepository.findById(id)
                .map(consultatie -> {
                    if (!"pacient".equals(consultatieDetails.getPacient().getRol())) {
                        throw new IllegalArgumentException("Invalid patient role");
                    }
                    if (!"doctor".equals(consultatieDetails.getDoctor().getRol())) {
                        throw new IllegalArgumentException("Invalid doctor role");
                    }
                    if (consultatieDetails.getAprobat() != true && consultatieDetails.getAprobat() != false) {
                        throw new IllegalArgumentException("Invalid approval status");
                    }
                    consultatie.setPacient(consultatieDetails.getPacient());
                    consultatie.setDoctor(consultatieDetails.getDoctor());
                    consultatie.setNota(consultatieDetails.getNota());
                    consultatie.setAprobat(consultatieDetails.getAprobat());
                    consultatie.setDataConsultatie(consultatieDetails.getDataConsultatie());
                    Consultatie updatedConsultatie = consultatieRepository.save(consultatie);
                    return ResponseEntity.ok(updatedConsultatie);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a consultation
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteConsultatie(@PathVariable Long id) {
        return consultatieRepository.findById(id)
                .map(consultatie -> {
                    consultatieRepository.delete(consultatie);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}