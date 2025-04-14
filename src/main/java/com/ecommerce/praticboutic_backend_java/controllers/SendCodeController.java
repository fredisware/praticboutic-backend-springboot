package com.ecommerce.praticboutic_backend_java.controllers;


import com.ecommerce.praticboutic_backend_java.services.EmailService;
import com.ecommerce.praticboutic_backend_java.utils.Utils;
import com.ecommerce.praticboutic_backend_java.entities.Identifiant;
import com.ecommerce.praticboutic_backend_java.repositories.IdentifiantRepository;
import com.ecommerce.praticboutic_backend_java.requests.SendCodeRequest;
import com.ecommerce.praticboutic_backend_java.responses.ErrorResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class SendCodeController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    IdentifiantRepository identifiantRepository;

    @Value("${application.url}")
    private String applicationUrl;

    @Value("${app.mail.from.address}")
    private String fromEmail;

    @Value("${app.mail.from.name}")
    private String fromName;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(SendCodeController.class);

    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendCodeRequest request) {
        try {
            // Generate random code
            SecureRandom secureRandom = new SecureRandom();
            int verificationCode = secureRandom.nextInt(999999);
            String formattedCode = String.format("%06d", verificationCode);

            // Encrypt the code (AES-256-CBC)
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            String encryptedCode = Utils.encryptCode(formattedCode, System.getenv("IDENTIFICATION_KEY"), iv);

            // Save to database
            Identifiant identifiant = new Identifiant(request.getEmail(), encryptedCode, 0);
            identifiantRepository.save(identifiant);

            // Send email
            sendEmail(request.getEmail(), formattedCode);

            // Return encrypted code and IV
            String encodedIv = Base64.getEncoder().encodeToString(iv);
            return ResponseEntity.ok(new String[]{encryptedCode, encodedIv});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private void sendEmail(String recipientEmail, String verificationCode) throws MessagingException, UnsupportedEncodingException {
        StringBuilder text = new StringBuilder();
        InputStream inputStream = EmailService.class.getClassLoader().getResourceAsStream("./static/logopbsvg.html");
        String logopb = "";
        if (inputStream != null) {
            try {
                logopb = new String(inputStream.readAllBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.error("InputStream est null !");
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String subject = "Votre code confidentiel";
        String htmlContent = "<html>" +
                "<body>" +
                logopb +
                "<p>Bonjour,</p>" +
                "<p>Voici le code de v&eacute;rification : " + verificationCode + "</p>" +
                "<p>Cordialement,<br>L'&eacute;quipe Praticboutic</p>" +
                "</body>" +
                "</html>";

        helper.setFrom(fromEmail, fromName);
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
