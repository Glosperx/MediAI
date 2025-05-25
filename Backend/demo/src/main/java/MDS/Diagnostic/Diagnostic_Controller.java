package MDS.Diagnostic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnoses")
public class Diagnostic_Controller {

    @Autowired
    private Diagnostic_Repository diagnosticRepository;

    //Get all diagnoses
    @GetMapping
    public List<Diagnostic> getAllDiagnoses() {
        return diagnosticRepository.findAll();
    }

    //Add
    @PostMapping
    public ResponseEntity<Diagnostic> createDiagnostic(@RequestBody Diagnostic diagnostic) {
        Diagnostic savedDiagnostic = diagnosticRepository.save(diagnostic);
        return ResponseEntity.ok(savedDiagnostic);
    }

    //Get by id
    @GetMapping("/{id}")
    public ResponseEntity<Diagnostic> getDiagnosticById(@PathVariable Long id) {
        return diagnosticRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Update by id
    @PutMapping("/update/{id}")
    public ResponseEntity<Diagnostic> updateDiagnostic(@PathVariable Long id, @RequestBody Diagnostic diagnosticDetails) {
        return diagnosticRepository.findById(id)
                .map(diagnostic -> {
                    diagnostic.setNume(diagnosticDetails.getNume());
                    diagnostic.setGravitate(diagnosticDetails.getGravitate());
                    Diagnostic updatedDiagnostic = diagnosticRepository.save(diagnostic);
                    return ResponseEntity.ok(updatedDiagnostic);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Delete by id
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDiagnostic(@PathVariable Long id) {
        return diagnosticRepository.findById(id)
                .map(diagnostic -> {
                    diagnosticRepository.delete(diagnostic);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}