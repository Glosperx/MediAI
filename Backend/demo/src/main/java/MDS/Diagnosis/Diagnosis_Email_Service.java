package MDS.Diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class Diagnosis_Email_Service {

    private final JavaMailSender mailSender;

    @Autowired
    public Diagnosis_Email_Service(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDiagnosisEmail(String email, Diagnosis_Response response) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("alexandrucojoaca1@gmail.com");
        mail.setTo(email);
        mail.setSubject("Rezultatul Analizei Simptomelor");

        StringBuilder sb = new StringBuilder();
        sb.append("Bună ziua!\n\n");
        sb.append("Rezultatul analizei tale este:\n\n");
        sb.append("Diagnostic: ").append(response.getDiagnosis()).append("\n");
        sb.append("Medicație recomandată: ").append(response.getMedication()).append("\n");
        sb.append("Simptome prezente: ").append(response.getIdentifiedSymptoms()).append("\n");
        sb.append("Simptome absente: ").append(response.getNegatedSymptoms()).append("\n");
        sb.append("Probabilități:\n");
        response.getProbabilities().forEach((k, v) ->
                sb.append("- ").append(k).append(": ").append(String.format("%.2f%%", v * 100)).append("\n")
        );
        sb.append("\nMultă sănătate!\nEchipa MediAI");

        mail.setText(sb.toString());
        mailSender.send(mail);
    }
}
