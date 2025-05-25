package MDS.Simptome;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
public class Simptom_Controller {

    @Autowired
    private Simptom_Repository simptomRepository;

    //Get all symptoms
    @GetMapping
    public List<Simptom> getAllSymptoms() {
        return simptomRepository.findAll();
    }

    //Add a new symphom
    @PostMapping
    public ResponseEntity<Simptom> createSymptom(@RequestBody Simptom simptom) {
        Simptom savedSimptom = simptomRepository.save(simptom);
        return ResponseEntity.ok(savedSimptom);
    }

    //Get a symptom ID
    @GetMapping("/{id}")
    public ResponseEntity<Simptom> getSymptomById(@PathVariable Long id) {
        return simptomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a symptom
    @PutMapping("/{id}")
    public ResponseEntity<Simptom> updateSymptom(@PathVariable Long id, @RequestBody Simptom simptomDetails) {
        return simptomRepository.findById(id)
                .map(simptom -> {
                    simptom.setNume(simptomDetails.getNume());
                    simptom.setGravitate(simptomDetails.getGravitate());
                    simptom.setDurata(simptomDetails.getDurata());
                    Simptom updatedSimptom = simptomRepository.save(simptom);
                    return ResponseEntity.ok(updatedSimptom);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSymptom(@PathVariable Long id) {
        return simptomRepository.findById(id)
                .map(simptom -> {
                    simptomRepository.delete(simptom);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}