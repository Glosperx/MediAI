// Controller pentru gestionarea consultatiilor
// Ofera endpoint-uri pentru operatii cu consultatii, inclusiv istoricul pacientilor

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

    // Clasa interna pentru modelul de vizualizare a consultatiilor
    private static class ConsultatieViewModel {
        private final Long id;
        private final User pacient;
        private final User doctor;
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
            this.dataConsultatie = consultatie.getDataConsultatie() != null ?
                    consultatie.getDataConsultatie().toLocalDateTime() : null;
            this.aprobat = consultatie.getAprobat();
            this.nota = consultatie.getNota();
        }

        // Getteri
        public Long getId() { return id; }
        public User getPacient() { return pacient; }
        public User getDoctor() { return doctor; }
        public LocalDateTime getDataConsultatie() { return dataConsultatie; }
        public Boolean getAprobat() { return aprobat; }
        public String getNota() { return nota; }
        public List<String> getSimptome() { return simptome; }
        public List<String> getDiagnostice() { return diagnostice; }
        public List<String> getMedicatii() { return medicatii; }

        // Setteri pentru liste
        public void setSimptome(List<String> simptome) { this.simptome = simptome; }
        public void setDiagnostice(List<String> diagnostice) { this.diagnostice = diagnostice; }
        public void setMedicatii(List<String> medicatii) { this.medicatii = medicatii; }
    }

    // Afiseaza pagina cu toate consultatiile pentru doctori si admini
    @GetMapping("/consultations")
    public String showConsultations(Model model) {
        try {
            logger.info("Accesare pagina consultatii");

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName()).orElseThrow();
            logger.info("Utilizator gasit: {}", currentUser.getEmail());

            // Permite acces doar pentru DOCTOR si ADMIN
            if (!("DOCTOR".equals(currentUser.getRol()) || "ADMIN".equals(currentUser.getRol()))) {
                logger.warn("Acces neautorizat de catre: {}", currentUser.getEmail());
                return "redirect:/predict";
            }

            // Adauga flag isAdmin in model
            model.addAttribute("isAdmin", "ADMIN".equals(currentUser.getRol()));

            List<Consultatie> consultations = consultatieRepository.findAll();
            logger.info("Gasite {} consultatii", consultations.size());

            List<ConsultatieViewModel> consultatiiView = new ArrayList<>();
            for (Consultatie consultatie : consultations) {
                ConsultatieViewModel viewModel = new ConsultatieViewModel(consultatie);

                // Obtine simptome
                List<Simptom_Pacient> simptome = simptomPacientRepository
                        .findByPacientAndDataRaportareGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setSimptome(simptome.stream()
                        .map(sp -> sp.getSimptom().getNume())
                        .collect(Collectors.toList()));

                // Obtine diagnostice
                List<Diagnostic_Pacient> diagnostice = diagnosticPacientRepository
                        .findByPacientAndDataDiagnosticGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setDiagnostice(diagnostice.stream()
                        .map(dp -> dp.getDiagnostic().getNume())
                        .collect(Collectors.toList()));

                // Obtine medicatii
                List<Medicatie_Pacient> medicatii = medicatiePacientRepository
                        .findByPacientAndDataPrescriereGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setMedicatii(medicatii.stream()
                        .map(mp -> mp.getPrescriptie().getNume())
                        .collect(Collectors.toList()));

                consultatiiView.add(viewModel);
            }

            model.addAttribute("consultations", consultatiiView);
            logger.info("Adaugate {} modele de consultatii in model", consultatiiView.size());

            return "consultations";
        } catch (Exception e) {
            logger.error("Eroare in showConsultations: ", e);
            throw e;
        }
    }

    // Afiseaza pagina cu istoricul consultatiilor pentru pacientul logat
    @GetMapping("/patient/history")
    public String showPatientHistory(Model model) {
        try {
            logger.info("Accesare pagina istoric pacient");

            // Obtine utilizatorul curent
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilizator nu a fost gasit"));
            logger.info("Utilizator gasit: {}", currentUser.getEmail());

            // Verifica daca utilizatorul este pacient
            if (!"PACIENT".equals(currentUser.getRol())) {
                logger.warn("Acces neautorizat la istoricul pacientului de catre: {}", currentUser.getEmail());
                model.addAttribute("error", "Acces interzis! Doar pacientii pot vedea istoricul consultatiilor.");
                return "error";
            }

            // Obtine consultatiile pacientului
            List<Consultatie> consultations = consultatieRepository.findByPacientUserId(currentUser.getUserId());
            logger.info("Gasite {} consultatii pentru pacientul {}", consultations.size(), currentUser.getEmail());

            // Creeaza modele de vizualizare pentru consultatii
            List<ConsultatieViewModel> consultatiiView = new ArrayList<>();
            for (Consultatie consultatie : consultations) {
                ConsultatieViewModel viewModel = new ConsultatieViewModel(consultatie);

                // Obtine simptome
                List<Simptom_Pacient> simptome = simptomPacientRepository
                        .findByPacientAndDataRaportareGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setSimptome(simptome.stream()
                        .map(sp -> sp.getSimptom().getNume())
                        .collect(Collectors.toList()));

                // Obtine diagnostice
                List<Diagnostic_Pacient> diagnostice = diagnosticPacientRepository
                        .findByPacientAndDataDiagnosticGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setDiagnostice(diagnostice.stream()
                        .map(dp -> dp.getDiagnostic().getNume())
                        .collect(Collectors.toList()));

                // Obtine medicatii
                List<Medicatie_Pacient> medicatii = medicatiePacientRepository
                        .findByPacientAndDataPrescriereGreaterThanEqual(consultatie.getPacient(), consultatie.getDataConsultatie());
                viewModel.setMedicatii(medicatii.stream()
                        .map(mp -> mp.getPrescriptie().getNume())
                        .collect(Collectors.toList()));

                consultatiiView.add(viewModel);
            }

            model.addAttribute("consultations", consultatiiView);
            logger.info("Adaugate {} modele de consultatii in model pentru pacient", consultatiiView.size());

            return "pacient_history"; // Returneaza template-ul html
        } catch (Exception e) {
            logger.error("Eroare in showPatientHistory: ", e);
            model.addAttribute("error", "Eroare la afisarea istoricului consultatiilor: " + e.getMessage());
            return "error";
        }
    }

    // Revizuieste o consultatie (aproba/dezaproba)
    @PostMapping("/consultations/{id}/review")
    public ResponseEntity<?> reviewConsultation(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String note) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilizator nu a fost gasit"));

            // Verifica autorizarea
            if (!(("DOCTOR".equals(currentUser.getRol()) || "ADMIN".equals(currentUser.getRol())))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "success", false,
                                "message", "Acces neautorizat"
                        ));
            }

            Consultatie consultatie = consultatieRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Consultatie nu a fost gasita"));

            // Verifica daca doctorul incearca sa dezaprobe o consultatie aprobata
            if ("DOCTOR".equals(currentUser.getRol()) &&
                    Boolean.TRUE.equals(consultatie.getAprobat()) &&
                    !approved) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "success", false,
                                "message", "Doctorii nu pot dezaproba consultatii aprobate"
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
                            "message", "Consultatia a fost actualizata cu succes",
                            "consultationId", updatedConsultatie.getId()
                    ));
        } catch (Exception e) {
            logger.error("Eroare la actualizarea consultatiei: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Eroare la actualizarea consultatiei: " + e.getMessage()
                    ));
        }
    }

    // Endpoint REST pentru toate consultatiile
    @GetMapping("/api/consultatie")
    @ResponseBody
    public List<Consultatie> getAllConsultatii() {
        return consultatieRepository.findAll();
    }

    // Endpoint REST pentru consultatiile unui pacient
    @GetMapping("/api/consultatie/pacient/{pacientId}")
    @ResponseBody
    public ResponseEntity<List<Consultatie>> getConsultatiiByPacient(@PathVariable Long pacientId) {
        return userRepository.findById(pacientId)
                .filter(user -> "PACIENT".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(consultatieRepository.findByPacientUserId(pacientId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Endpoint REST pentru consultatiile unui doctor
    @GetMapping("/api/consultatie/doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<List<Consultatie>> getConsultatiiByDoctor(@PathVariable Long doctorId) {
        return userRepository.findById(doctorId)
                .filter(user -> "DOCTOR".equals(user.getRol()))
                .map(user -> ResponseEntity.ok(consultatieRepository.findByDoctorUserId(doctorId)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Endpoint REST pentru crearea unei consultatii
    @PostMapping("/api/consultatie")
    @ResponseBody
    public ResponseEntity<Consultatie> createConsultatie(@RequestBody Consultatie consultatie) {
        if (consultatie.getPacient() == null || !"PACIENT".equals(consultatie.getPacient().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        if (consultatie.getDoctor() == null || !"DOCTOR".equals(consultatie.getPacient().getRol())) {
            return ResponseEntity.badRequest().build();
        }
        Consultatie savedConsultatie = consultatieRepository.save(consultatie);
        return ResponseEntity.ok(savedConsultatie);
    }

    // Endpoint REST pentru actualizarea unei consultatii
    @PutMapping("/api/consultatie/update/{id}")
    @ResponseBody
    public ResponseEntity<Consultatie> updateConsultatie(@PathVariable Long id, @RequestBody Consultatie consultatieDetails) {
        return consultatieRepository.findById(id)
                .map(consultatie -> {
                    if (!"PACIENT".equals(consultatieDetails.getPacient().getRol())) {
                        throw new IllegalArgumentException("Rol pacient invalid");
                    }
                    if (!"DOCTOR".equals(consultatieDetails.getDoctor().getRol())) {
                        throw new IllegalArgumentException("Rol doctor invalid");
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

    // Endpoint REST pentru stergerea unei consultatii
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