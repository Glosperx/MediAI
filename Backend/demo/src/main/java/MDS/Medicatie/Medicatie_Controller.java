package MDS.Medicatie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicatie")
public class Medicatie_Controller {

    @Autowired
    private Medicatie_Repository medicatieRepository;

    //Create a prescription
    @PostMapping
    public Medicatie createMedicatie(@RequestBody Medicatie medicatie) {
        return medicatieRepository.save(medicatie);
    }

    //Get all the prescriptions
    @GetMapping
    public List<Medicatie> getAllMedicatie() {
        return medicatieRepository.findAll();
    }

    //Get after id
    @GetMapping("/{id}")
    public ResponseEntity<Medicatie> getMedicatieById(@PathVariable Long id) {
        Optional<Medicatie> medicatie = medicatieRepository.findById(id);
        return medicatie.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Update
    @PutMapping("/update/{id}")
    public ResponseEntity<Medicatie> updateMedicatie(@PathVariable Long id, @RequestBody Medicatie updatedMedicatie) {
        return medicatieRepository.findById(id)
                .map(existingMedicatie -> {
                    existingMedicatie.setNume(updatedMedicatie.getNume());
                    existingMedicatie.setDurata(updatedMedicatie.getDurata());
                    existingMedicatie.setTipAdministrare(updatedMedicatie.getTipAdministrare());
                    Medicatie savedMedicatie = medicatieRepository.save(existingMedicatie);
                    return ResponseEntity.ok(savedMedicatie);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Șterge o prescripție
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedicatie(@PathVariable Long id) {
        if (medicatieRepository.existsById(id)) {
            medicatieRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}