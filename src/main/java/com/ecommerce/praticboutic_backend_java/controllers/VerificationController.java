package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.configurations.EmailConfig;
import com.ecommerce.praticboutic_backend_java.requests.VerificationRequest;
import com.ecommerce.praticboutic_backend_java.responses.VerificationResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class VerificationController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${identification.key}")
    private String identificationKey;

    @Value("${application.url}")
    private String applicationUrl;

    @Value("${mail.sender.address}")
    private String senderEmail;

    @Value("${mail.sender.name}")
    private String senderName;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody VerificationRequest request) {
        try {
            // Vérifier si l'email existe déjà
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM client WHERE email = ?",
                    Integer.class,
                    request.getEmail());

            if (count != null && count > 0) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Le courriel " + request.getEmail() + " est déjà attribué à un client. Impossible de continuer.");
            }

            // Générer un hash unique
            String hash = generateHash();

            // Insérer l'identifiant dans la base de données
            jdbcTemplate.update(
                    "INSERT INTO identifiant(email, hash, actif) VALUES (?, ?, ?)",
                    request.getEmail(), hash, 0);

            // Générer un code de vérification à 6 chiffres
            String verificationCode = generateVerificationCode();

            // Chiffrer le code
            byte[] iv = generateIV();
            String encryptedCode = encryptCode(verificationCode, iv);

            // Envoyer l'email
            sendEmail(request.getEmail(), verificationCode);

            // Créer la réponse
            VerificationResponse response = new VerificationResponse();
            response.setEncryptedCode(encryptedCode);
            response.setIv(Base64.getEncoder().encodeToString(iv));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    private String generateHash() {
        return Long.toHexString(Double.doubleToLongBits(Math.random() * System.currentTimeMillis()));
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(1000000);
        return String.format("%06d", code);
    }

    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16]; // 16 bytes for AES
        random.nextBytes(iv);
        return iv;
    }

    private String encryptCode(String code, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(identificationKey.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(code.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private void sendEmail(String recipientEmail, String code) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(senderEmail, senderName);
        helper.setTo(recipientEmail);
        helper.setSubject("Votre code confidentiel");

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<link href='https://fonts.googleapis.com/css?family=Public+Sans' rel='stylesheet'>" +
                "</head>" +
                "<body>" +
                "<img src='" + applicationUrl + "/common/img/logo.png' width='253' height='114' alt=''>" +
                "<br><br>" +
                "<p style=\"font-family: 'Public+Sans'\">Bonjour " +
                recipientEmail + "<br><br>" +
                "Voici le code de vérification : " + code +
                "<br>" +
                "Cordialement<br><br>L'équipe praticboutic<br><br></p>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}