package MDS.Users;

import MDS.Analize.Analize_Repository;
import MDS.Consultatie.Consultatie_Repository;
import MDS.Diagnostic_Pacient.Diagnostic_Pacient_Repository;
import MDS.Medicatie_Pacient.Medicatie_Pacient_Repository;
import MDS.Simptome_Pacient.Simptom_Pacient_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class User_Controller {

    @Autowired
    private User_Repository userRepository;

    @Autowired
    private Consultatie_Repository consultatieRepository;
    @Autowired
    private Medicatie_Pacient_Repository medicatiePacientRepository;
    @Autowired
    private Analize_Repository analizeRepository;
    @Autowired
    private Simptom_Pacient_Repository simptomePacientRepository;
    @Autowired
    private Diagnostic_Pacient_Repository diagnosticPacientRepository;

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get all patients
    @GetMapping("/pacienti")
    public List<User> getPacienti() {
        return userRepository.findByRol("pacient");
    }

    // Get all doctors
    @GetMapping("/doctori")
    public List<User> getDoctori() {
        return userRepository.findByRol("doctor");
    }

    // Get all admins
    @GetMapping("/admini")
    public List<User> getAdmini() {
        return userRepository.findByRol("admin");
    }

    // Add a new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (!List.of("pacient", "doctor", "admin").contains(user.getRol())) {
            return ResponseEntity.badRequest().build();
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // Get a user by id
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get a user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a user
    @PutMapping("/update/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setAcronim(userDetails.getAcronim());
                    user.setRol(userDetails.getRol());
                    user.setEmail(userDetails.getEmail());
                    user.setParola(userDetails.getParola());
                    if (!List.of("pacient", "doctor", "admin").contains(user.getRol())) {
                        throw new IllegalArgumentException("Invalid role");
                    }
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    try {
                        // Delete dependent records
                        consultatieRepository.deleteByPacientUserId(id);
                        consultatieRepository.deleteByDoctorUserId(id);
                        medicatiePacientRepository.deleteByPacientUserId(id);
                        medicatiePacientRepository.deleteByDoctorUserId(id);
                        analizeRepository.deleteByUserId(id);
                        simptomePacientRepository.deleteByPacientUserId(id);
                        diagnosticPacientRepository.deleteByPacientUserId(id);
                        diagnosticPacientRepository.deleteByDoctorUserId(id);

                        // Delete the user
                        userRepository.delete(user);
                        return ResponseEntity.ok("User deleted successfully");
                    } catch (DataIntegrityViolationException e) {
                        return ResponseEntity.badRequest()
                                .body("Cannot delete user due to associated records");
                    } catch (Exception e) {
                        return ResponseEntity.status(500)
                                .body("Unexpected error: " + e.getMessage());
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}