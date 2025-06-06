// Controller pentru gestionarea trimiterii emailurilor
// Ofera endpoint pentru trimiterea mesajelor prin email

package MDS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private JavaMailSender mailSender;

    // Trimite un email
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailRequest.getTo());
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getMessage());
            message.setFrom("alexandrucojoaca1@gmail.com");

            mailSender.send(message);

            return ResponseEntity.ok("Email trimis cu succes!");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Eroare la trimiterea emailului: " + e.getMessage());
        }
    }

    // Clasa pentru datele cererii de email
    public static class EmailRequest {
        private String to;
        private String subject;
        private String message;

        // Constructori
        public EmailRequest() {}

        public EmailRequest(String to, String subject, String message) {
            this.to = to;
            this.subject = subject;
            this.message = message;
        }

        // Getteri si setteri
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}