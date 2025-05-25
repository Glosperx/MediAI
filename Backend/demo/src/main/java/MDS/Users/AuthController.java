package MDS.Users;

import MDS.Diagnosis.Diagnosis_Email_Service;
import MDS.Diagnosis.Diagnosis_Request;
import MDS.Diagnosis.Diagnosis_Response;
import MDS.Diagnosis.Diagnosis_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    private final Diagnosis_Email_Service diagnosisEmailService;
    private final User_Service userService;
    private final VerificationTokenRepository tokenRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final Diagnosis_Service diagnosisService;

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


    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String acronim,
            @RequestParam String email,
            @RequestParam String parola,
            @RequestParam String rol,
            Model model
    ) {
        System.out.println("Începe procesarea înregistrării pentru email: " + email);

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

        if (!rol.equals("PACIENT") && !rol.equals("DOCTOR")) {
            model.addAttribute("error", "Rol invalid! Selectează PACIENT sau DOCTOR.");
            return "register";
        }

        if (userService.existsByEmail(email)) {
            System.out.println("Emailul există deja: " + email);
            model.addAttribute("error", "Emailul există deja!");
            return "register";
        }

        try {
            String encodedPassword = passwordEncoder.encode(parola);
            User user = new User(acronim, rol, email, encodedPassword);
            user.setActiv(false);
            userService.save(user);
            System.out.println("Utilizator salvat: " + email);

            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setEmail(email);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            tokenRepo.save(verificationToken);
            System.out.println("Token salvat pentru email: " + email);

            String verificationLink = "http://localhost:8080/verify?token=" + token;
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("alexandrucojoaca1@gmail.com");
            mail.setTo(email);
            mail.setSubject("Verifică contul tău");
            mail.setText("Bună ziua!\n\n" +
                    "Pentru a activa contul, te rugăm să accesezi următorul link:\n" +
                    verificationLink + "\n\n" +
                    "Linkul este valabil 24 de ore.\n\n" +
                    "Dacă nu ai creat acest cont, ignoră acest email.\n\n" +
                    "Cu stimă,\nEchipa MediAI");

            mailSender.send(mail);
            System.out.println("Email trimis către: " + email);

            model.addAttribute("message", "Înregistrarea a fost realizată cu succes! Verifică-ți emailul pentru linkul de activare!");
            return "redirect:/custom-login?verify_email";
        } catch (Exception e) {
            System.err.println("Eroare la înregistrare: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "A apărut o eroare la înregistrare. Încearcă din nou.");
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        System.out.println("Încercare de verificare cu token: " + token);

        Optional<VerificationToken> verificationTokenOpt = tokenRepo.findByToken(token);
        if (verificationTokenOpt.isEmpty()) {
            System.out.println("Token invalid: " + token);
            return "redirect:/custom-login?invalid_token";
        }

        VerificationToken verificationToken = verificationTokenOpt.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            System.out.println("Token expirat: " + token);
            return "redirect:/custom-login?expired_token";
        }

        Optional<User> userOpt = userService.findByEmail(verificationToken.getEmail());
        if (userOpt.isEmpty()) {
            System.out.println("Utilizator nu a fost găsit pentru email: " + verificationToken.getEmail());
            return "redirect:/custom-login?user_not_found";
        }

        User user = userOpt.get();
        user.setActiv(true);
        userService.save(user);
        System.out.println("Utilizator activat: " + user.getEmail());

        tokenRepo.delete(verificationToken);
        return "redirect:/custom-login?verified";
    }

    @GetMapping("/custom-login")
    public String showLoginPage(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                @RequestParam(required = false) String verify_email,
                                @RequestParam(required = false) String verified,
                                @RequestParam(required = false) String expired_token,
                                @RequestParam(required = false) String invalid_token,
                                @RequestParam(required = false) String user_not_found,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Email sau parolă incorectă!");
        }
        if (logout != null) {
            model.addAttribute("message", "Te-ai delogat cu succes!");
        }
        if (verify_email != null) {
            model.addAttribute("message", "Verifică-ți emailul pentru linkul de activare!");
        }
        if (verified != null) {
            model.addAttribute("message", "Cont activat cu succes! Te poți loga acum.");
        }
        if (expired_token != null) {
            model.addAttribute("error", "Linkul de activare a expirat. Te rugăm să te înregistrezi din nou.");
        }
        if (invalid_token != null) {
            model.addAttribute("error", "Link de activare invalid. Te rugăm să te înregistrezi din nou.");
        }
        if (user_not_found != null) {
            model.addAttribute("error", "Utilizatorul nu a fost găsit. Te rugăm să te înregistrezi din nou.");
        }
        return "login";
    }

    @GetMapping("/predict")
    public String showPredictForm(Model model) {
        model.addAttribute("diagnosisRequest", new Diagnosis_Request());
        return "predict";
    }

    @PostMapping("/predict")
    public String submitPrediction(@ModelAttribute("diagnosisRequest") Diagnosis_Request request,
                                   Model model) {
        try {
            Diagnosis_Response response = diagnosisService.getDiagnosis(request);
            model.addAttribute("prediction", response);

            // Cea mai simplă variantă de a obține emailul utilizatorului logat
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            diagnosisEmailService.sendDiagnosisEmail(email, response);

            return "predict";
        } catch (Exception e) {
            model.addAttribute("error", "Eroare la obținerea predicției: " + e.getMessage());
            return "predict";
        }
    }

}