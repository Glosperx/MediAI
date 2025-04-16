package MDS.Analize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analize")
public class Analize_Controller {

    @Autowired
    private Analize_Repository analizeRepository;

    // Get all analyses
    @GetMapping
    public List<Analize> getAllAnalize() {
        return analizeRepository.findAll();
    }

    // Add
    @PostMapping
    public ResponseEntity<Analize> createAnaliza(@RequestBody Analize analiza) {
        Analize savedAnaliza = analizeRepository.save(analiza);
        return ResponseEntity.ok(savedAnaliza);
    }

    // Get
    @GetMapping("/{id}")
    public ResponseEntity<Analize> getAnalizaById(@PathVariable Long id) {
        return analizeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/update/{id}")
    public ResponseEntity<Analize> updateAnaliza(@PathVariable Long id, @RequestBody Analize analizaDetails) {
        return analizeRepository.findById(id)
                .map(analiza -> {
                    analiza.setUserId(analizaDetails.getUserId());
                    analiza.setTipAnaliza(analizaDetails.getTipAnaliza());
                    analiza.setDataAnaliza(analizaDetails.getDataAnaliza());
                    analiza.setRezultat(analizaDetails.getRezultat());
                    Analize updatedAnaliza = analizeRepository.save(analiza);
                    return ResponseEntity.ok(updatedAnaliza);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAnaliza(@PathVariable Long id) {
        return analizeRepository.findById(id)
                .map(analiza -> {
                    analizeRepository.delete(analiza);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}