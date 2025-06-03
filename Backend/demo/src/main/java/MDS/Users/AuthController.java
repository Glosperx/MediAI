package MDS.Users;

import MDS.Diagnosis.Diagnosis_Email_Service;
import MDS.Diagnosis.Diagnosis_Request;
import MDS.Diagnosis.Diagnosis_Response;
import MDS.Diagnosis.Diagnosis_Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static javax.crypto.Cipher.SECRET_KEY;

/**
 * Controller principal pentru gestionarea autentificarii si inregistrarii utilizatorilor
 * Gestioneaza: inregistrare, verificare email, login, dashboard, predictii medicale
 */
@Controller
public class AuthController {

    // Logger pentru inregistrarea evenimentelor si erorilor
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Cheie secreta pentru crearea conturilor de admin (PROBLEMA DE SECURITATE - hardcodata)
    private static final String SECRET_KEY = "Ciscosecpa55";

    // Servicii injectate prin dependency injection
    private final Diagnosis_Email_Service diagnosisEmailService; // Serviciu pentru trimiterea emailurilor cu diagnostice
    private final User_Service userService; // Serviciu pentru gestionarea utilizatorilor
    private final VerificationTokenRepository tokenRepo; // Repository pentru token-urile de verificare
    private final JavaMailSender mailSender; // Serviciu pentru trimiterea emailurilor
    private final PasswordEncoder passwordEncoder; // Encoder pentru criptarea parolelor
    private final Diagnosis_Service diagnosisService; // Serviciu pentru diagnostice medicale

    /**
     * Constructor cu toate dependentele injectate
     */
    @Autowired
    public AuthController(User_Service userService,
                          VerificationTokenRepository tokenRepo,
                          JavaMailSender mailSender,
                          PasswordEncoder passwordEncoder,
                          Diagnosis_Service diagnosisService,
                          Diagnosis_Email_Service diagnosisEmailService) {
        this.userService = userService;
        this.tokenRepo = tokenRepo;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.diagnosisService = diagnosisService;
        this.diagnosisEmailService = diagnosisEmailService;
    }

    /**
     * Afiseaza pagina de inregistrare
     * GET /register
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        return "register"; // Returneaza template-ul "register.html"
    }

    /**
     * Proceseaza inregistrarea unui utilizator nou
     * POST /register
     */
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String acronim,  // Numele/acronimul utilizatorului
            @RequestParam String email,    // Adresa de email
            @RequestParam String parola,   // Parola
            @RequestParam String rol,      // Rolul: PACIENT sau DOCTOR
            Model model
    ) {
        System.out.println("Incepe procesarea inregistrarii pentru email: " + email);

        // Validarea datelor de intrare
        if (acronim == null || acronim.trim().isEmpty()) {
            model.addAttribute("error", "Acronimul este obligatoriu!");
            return "register";
        }

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Emailul este obligatoriu!");
            return "register";
        }

        if (parola == null || parola.trim().isEmpty()) {
            model.addAttribute("error", "Parola este obligatorie!");
            return "register";
        }

        if (rol == null || rol.trim().isEmpty()) {
            model.addAttribute("error", "Rolul este obligatoriu!");
            return "register";
        }

        // Verificarea ca rolul este valid (doar PACIENT sau DOCTOR)
        if (!rol.equals("PACIENT") && !rol.equals("DOCTOR")) {
            model.addAttribute("error", "Rol invalid! Selecteaza PACIENT sau DOCTOR.");
            return "register";
        }

        // Verificarea ca emailul nu exista deja in sistem
        if (userService.existsByEmail(email)) {
            System.out.println("Emailul existe deja: " + email);
            model.addAttribute("error", "Emailul existe deja!");
            return "register";
        }

        try {
            // Criptarea parolei inainte de salvare
            String encodedPassword = passwordEncoder.encode(parola);
            User user = new User(acronim, rol, email, encodedPassword);
            user.setActiv(false); // Contul nu este activ pana la verificarea emailului
            userService.save(user);
            System.out.println("Utilizator salvat: " + email);

            // Crearea token-ului de verificare pentru email
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setEmail(email);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token valid 24h
            tokenRepo.save(verificationToken);
            System.out.println("Token salvat pentru email: " + email);

            // Trimiterea emailului de verificare
            String verificationLink = "http://localhost:8080/verify?token=" + token;
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("alexandrucojoaca1@gmail.com");
            mail.setTo(email);
            mail.setSubject("Verifica contul tau");
            mail.setText("Buna ziua!\n\n" +
                    "Pentru a activa contul, te rugam sa accesezi urmatorul link:\n" +
                    verificationLink + "\n\n" +
                    "Linkul este valabil 24 de ore.\n\n" +
                    "Daca nu ai creat acest cont, ignora acest email.\n\n" +
                    "Cu stima,\nEchipa MediAI");

            mailSender.send(mail);
            System.out.println("Email trimis catre: " + email);

            model.addAttribute("message", "Inregistrarea a fost realizata cu succes! Verifica-ti emailul pentru linkul de activare!");
            return "redirect:/custom-login?verify_email";
        } catch (Exception e) {
            System.err.println("Eroare la inregistrare: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "A aparut o eroare la inregistrare. Incearca din nou.");
            return "register";
        }
    }

    /**
     * Verifica token-ul de email si activeaza contul utilizatorului
     * GET /verify?token=...
     */
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        System.out.println("Incercare de verificare cu token: " + token);

        // Cautarea token-ului
        Optional<VerificationToken> verificationTokenOpt = tokenRepo.findByToken(token);
        if (verificationTokenOpt.isEmpty()) {
            System.out.println("Token invalid: " + token);
            return "redirect:/custom-login?invalid_token";
        }

        VerificationToken verificationToken = verificationTokenOpt.get();

        // Verificarea daca token-ul nu a expirat
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            System.out.println("Token expirat: " + token);
            return "redirect:/custom-login?expired_token";
        }

        // Cautarea utilizatorului asociat cu token-ul
        Optional<User> userOpt = userService.findByEmail(verificationToken.getEmail());
        if (userOpt.isEmpty()) {
            System.out.println("Utilizator nu a fost gasit pentru email: " + verificationToken.getEmail());
            return "redirect:/custom-login?user_not_found";
        }

        // Activarea contului utilizatorului
        User user = userOpt.get();
        user.setActiv(true);
        userService.save(user);
        System.out.println("Utilizator activat: " + user.getEmail());

        // Stergerea token-ului dupa utilizare
        tokenRepo.delete(verificationToken);
        return "redirect:/custom-login?verified";
    }

    /**
     * Afiseaza pagina de login cu diverse mesaje (erori, succese)
     * GET /custom-login
     */
    @GetMapping("/custom-login")
    public String showLoginPage(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                @RequestParam(required = false) String verify_email,
                                @RequestParam(required = false) String verified,
                                @RequestParam(required = false) String expired_token,
                                @RequestParam(required = false) String invalid_token,
                                @RequestParam(required = false) String user_not_found,
                                Model model) {
        // Adaugarea mesajelor corespunzatoare in functie de parametrii URL
        if (error != null) {
            model.addAttribute("error", "Email sau parola incorecta!");
        }
        if (logout != null) {
            model.addAttribute("message", "Te-ai delogat cu succes!");
        }
        if (verify_email != null) {
            model.addAttribute("message", "Verifica-ti emailul pentru linkul de activare!");
        }
        if (verified != null) {
            model.addAttribute("message", "Cont activat cu succes! Te poti loga acum.");
        }
        if (expired_token != null) {
            model.addAttribute("error", "Linkul de activare a expirat. Te rugam sa te inregistrezi din nou.");
        }
        if (invalid_token != null) {
            model.addAttribute("error", "Link de activare invalid. Te rugam sa te inregistrezi din nou.");
        }
        if (user_not_found != null) {
            model.addAttribute("error", "Utilizatorul nu a fost gasit. Te rugam sa te inregistrezi din nou.");
        }
        return "login";
    }

    /**
     * Dashboard principal care redirectioneaza utilizatorii in functie de rol
     * GET /dashboard
     */
    @GetMapping("/dashboard")
    public String showDashboard() {
        // Obtinerea utilizatorului curent din contextul de securitate
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizator nu a fost gasit"));

        // Redirectionarea in functie de rolul utilizatorului
        switch (currentUser.getRol()) {
            case "PACIENT":
                return "redirect:/predict"; // Pacientii merg la pagina de predictii
            case "DOCTOR":
                return "redirect:/consultations"; // Doctorii merg la consultatii
            case "ADMIN":
                return "redirect:/consultations"; // Adminii merg la consultatii
            default:
                return "redirect:/custom-login?error";
        }
    }

    /**
     * Afiseaza formularul de predictie medicala (doar pentru pacienti)
     * GET /predict
     */
    @GetMapping("/predict")
    public String showPredictForm(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizator nu a fost gasit"));

        // Verificarea ca doar pacientii pot accesa predictiile
        if (!"PACIENT".equals(currentUser.getRol())) {
            if ("DOCTOR".equals(currentUser.getRol())) {
                return "redirect:/consultations";
            }
            model.addAttribute("error", "Accesul interzis! Doar pacientii pot efectua predictii.");
            return "error";
        }

        model.addAttribute("diagnosisRequest", new Diagnosis_Request());
        return "predict";
    }

    /**
     * Proceseaza cererea de predictie medicala
     * POST /predict
     */
    @PostMapping("/predict")
    public String submitPrediction(@ModelAttribute("diagnosisRequest") Diagnosis_Request request,
                                   Model model) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilizator nu a fost gasit"));

            // Verificarea ca utilizatorul este pacient
            if (!"PACIENT".equals(currentUser.getRol())) {
                model.addAttribute("error", "Doar pacientii pot efectua predictii!");
                return "error";
            }

            // Obtinerea diagnosticului de la serviciul de AI
            Diagnosis_Response response = diagnosisService.getDiagnosis(request);
            model.addAttribute("prediction", response);

            // Trimiterea diagnosticului pe email
            diagnosisEmailService.sendDiagnosisEmail(email, response);

            // Salvarea diagnosticului in baza de date
            Long pacientId = currentUser.getUserId();
            Diagnosis_Response response1 = diagnosisService.getDiagnosisAndSave(request, pacientId);
            model.addAttribute("prediction", response1);

            return "predict";
        } catch (Exception e) {
            model.addAttribute("error", "Eroare la obtinerea predictiei: " + e.getMessage());
            return "predict";
        }
    }

    /**
     * Metoda helper pentru verificarea daca utilizatorul curent este doctor
     */
    private boolean isDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String email = auth.getName();
            return userService.findByEmail(email)
                    .map(user -> "DOCTOR".equals(user.getRol()))
                    .orElse(false);
        }
        return false;
    }

    /**
     * API endpoint pentru crearea unui cont de admin
     * POST /api/auth/create-admin
     * ATENTIE: Acest endpoint prezinta riscuri de securitate!
     */
    @PostMapping("/api/auth/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody CreateAdminRequest request) {
        // Verificarea cheii secrete (PROBLEMATICA - cheia este hardcodata)
        if (!SECRET_KEY.equals(request.getSecretKey())) {
            logger.warn("Incercare de creare admin cu cheie invalida");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Cheie secreta invalida"));
        }

        try {
            // Crearea contului de admin
            User admin = userService.createAdmin(request.getEmail(), request.getPassword());

            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Admin creat cu succes",
                            "email", admin.getEmail()
                    ));
        } catch (IllegalArgumentException e) {
            logger.error("Eroare la validare: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Eroare neasteptata la crearea adminului", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Eroare interna la crearea adminului"));
        }
    }

}