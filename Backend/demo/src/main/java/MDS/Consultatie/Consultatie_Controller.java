package MDS.Consultatie;

import MDS.Diagnostic_Pacient.Diagnostic_Pacient;
import MDS.Diagnostic_Pacient.Diagnostic_Pacient_Repository;
import MDS.Medicatie_Pacient.Medicatie_Pacient;
import MDS.Medicatie_Pacient.Medicatie_Pacient_Repository;
import MDS.Simptome_Pacient.Simptom_Pacient;
import MDS.Simptome_Pacient.Simptom_Pacient_Repository;
import MDS.Users.User;
import MDS.Users.User_Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class Consultatie_Controller {
    private static final Logger logger = LoggerFactory.getLogger(Consultatie_Controller.class);


    @Autowired
    private Consultatie_Repository consultatieRepository;

    @Autowired
    private User_Repository userRepository;

    @Autowired
    private Diagnostic_Pacient_Repository diagnosticPacientRepository;

    @Autowired
    private Medicatie_Pacient_Repository medicatiePacientRepository;

    @Autowired
    private Simptom_Pacient_Repository simptomPacientRepository;

    private static class ConsultatieViewModel {
        private final Long id;
        private final User pacient;
        private final User doctor;
//        private final Timestamp dataConsultatie;
        private final LocalDateTime dataConsultatie;
        private final Boolean aprobat;
        private final String nota;
        private List<String> simptome = new ArrayList<>();
        private List<String> diagnostice = new ArrayList<>();
        private List<String> medicatii = new ArrayList<>();

        public ConsultatieViewModel(Consultatie consultatie) {
            this.id = consultatie.getId();
            this.pacient = consultatie.getPacient();
            this.doctor = consultatie.getDoctor();
//            this.dataConsultatie = consultatie.getDataConsultatie();
            this.dataConsultatie = consultatie.getDataConsultatie() != null ?
                    consultatie.getDataConsultatie().toLocalDateTime() : null; // Convertim Timestamp în LocalDateTime

            this.aprobat = consultatie.getAprobat();
            this.nota = consultatie.getNota();
        }

        // Getters
        public Long getId() { return id; }
        public User getPacient() { return pacient; }
        public User getDoctor() { return doctor; }
//        public Timestamp getDataConsultatie() { return dataConsultatie; }
public LocalDateTime getDataConsultatie() { return dataConsultatie; }
        public Boolean getAprobat() { return aprobat; }
        public String getNota() { return nota; }
        public List<String> getSimptome() { return simptome; }
        public List<String> getDiagnostice() { return diagnostice; }
        public List<String> getMedicatii() { return medicatii; }

        // Setters pentru liste
        public void setSimptome(List<String> simptome) { this.simptome = simptome; }
        public void setDiagnostice(List<String> diagnostice) { this.diagnostice = diagnostice; }
        public void setMedicatii(List<String> medicatii) { this.medicatii = medicatii; }
    }

    @GetMapping("/consultations")
    public String showConsultations(Model model) {
        try {
            logger.info("Accessing consultations page");

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName()).orElseThrow();
            logger.info("User found: {}", currentUser.getEmail());

            // Allow both DOCTOR and ADMIN roles
            if (!("DOCTOR".equals(currentUser.getRol()) || "ADMIN".equals(currentUser.getRol()))) {
                logger.warn("Unauthorized access attempt by: {}", currentUser.getEmail());
                return "redirect:/predict";
            }

            // Add isAdmin flag to model
            model.addAttribute("isAdmin", "ADMIN".equals(currentUser.getRol()));

            List<Consultatie> consultations = consultatieRepository.findAll();
            logger.info("Found {} consultations", consultations.size());

            List<ConsultatieViewModel> consultatiiView = new ArrayList<>();
            for (Consultatie consultatie : consultations) {
                ConsultatieViewModel viewModel = new ConsultatieViewModel(consultatie);

                // Get symptoms
                List<Simptom_Pacient> simptome = simptomPacientRepository
                        .findByPacientAndDataRaportareGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setSimptome(simptome.stream()
                        .map(sp -> sp.getSimptom().getNume())
                        .collect(Collectors.toList()));

                // Get diagnoses
                List<Diagnostic_Pacient> diagnostice = diagnosticPacientRepository
                        .findByPacientAndDataDiagnosticGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setDiagnostice(diagnostice.stream()
                        .map(dp -> dp.getDiagnostic().getNume())
                        .collect(Collectors.toList()));

                // Get medications
                List<Medicatie_Pacient> medicatii = medicatiePacientRepository
                        .findByPacientAndDataPrescriereGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setMedicatii(medicatii.stream()
                        .map(mp -> mp.getPrescriptie().getNume())
                        .collect(Collectors.toList()));

                consultatiiView.add(viewModel);
            }

            model.addAttribute("consultations", consultatiiView);
            logger.info("Added {} consultation view models to the model", consultatiiView.size());

            return "consultations";
        } catch (Exception e) {
            logger.error("Error in showConsultations: ", e);
            throw e;
        }
    }

    @PostMapping("/consultations/{id}/review")
    public ResponseEntity<?> reviewConsultation(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String note) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is authorized
            if (!(("DOCTOR".equals(currentUser.getRol()) || "ADMIN".equals(currentUser.getRol())))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "success", false,
                                "message", "Unauthorized access"
                        ));
            }

            Consultatie consultatie = consultatieRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Consultation not found"));

            // If user is a doctor and trying to disapprove an approved consultation
            if ("DOCTOR".equals(currentUser.getRol()) &&
                    Boolean.TRUE.equals(consultatie.getAprobat()) && // Changed this line
                    !approved) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "success", false,
                                "message", "Doctors cannot disapprove approved consultations"
                        ));
            }

            consultatie.setAprobat(approved);
            consultatie.setNota(note);
            consultatie.setDoctor(currentUser);
            consultatie.setDataConsultatie(Timestamp.valueOf(LocalDateTime.now()));

            Consultatie updatedConsultatie = consultatieRepository.save(consultatie);

            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "message", "Consultația a fost actualizată cu succes",
                            "consultationId", updatedConsultatie.getId()
                    ));
        } catch (Exception e) {
            logger.error("Error updating consultation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Eroare la actualizarea consultației: " + e.getMessage()
                    ));
        }
    }

    // REST API endpoints
    @GetMapping("/api/consultatie")
    @ResponseBody
    public List<Consultatie> getAllConsultatii() {
        return consultatieRepository.findAll();
    }

    @GetMapping("/api/consultatie/pacient/{pacientId}")
    @ResponseBody
    public ResponseEntity<List<Consultatie>> getConsultatiiByPacient(@PathVariable Long pacientId) {
        return userRepository.findById(pacientId)
                .filter(user -> "PACIENT".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(consultatieRepository.findByPacientUserId(pacientId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/api/consultatie/doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<List<Consultatie>> getConsultatiiByDoctor(@PathVariable Long doctorId) {
        return userRepository.findById(doctorId)
                .filter(user -> "DOCTOR".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(consultatieRepository.findByDoctorUserId(doctorId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/api/consultatie")
    @ResponseBody
    public ResponseEntity<Consultatie> createConsultatie(@RequestBody Consultatie consultatie) {
        if (consultatie.getPacient() == null || !"PACIENT".equals(consultatie.getPacient().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        if (consultatie.getDoctor() == null || !"DOCTOR".equals(consultatie.getDoctor().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        Consultatie savedConsultatie = consultatieRepository.save(consultatie);
        return ResponseEntity.ok(savedConsultatie);
    }

    @PutMapping("/api/consultatie/update/{id}")
    @ResponseBody
    public ResponseEntity<Consultatie> updateConsultatie(@PathVariable Long id, @RequestBody Consultatie consultatieDetails) {
        return consultatieRepository.findById(id)
                .map(consultatie -> {
                    if (!"PACIENT".equals(consultatieDetails.getPacient().getRol())) {
                        throw new IllegalArgumentException("Invalid patient role");
                    }
                    if (!"DOCTOR".equals(consultatieDetails.getDoctor().getRol())) {
                        throw new IllegalArgumentException("Invalid doctor role");
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

    @DeleteMapping("/api/consultatie/delete/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteConsultatie(@PathVariable Long id) {
        return consultatieRepository.findById(id)
                .map(consultatie -> {
                    consultatieRepository.delete(consultatie);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}