package com.ecommerce.praticboutic_backend_java.services;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import jakarta.mail.Message.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;


@Service
public class EmailService
{

    @Value("${app.mail.from.address}")
    private String fromEmail;
    
    @Value("${app.mail.from.name}")
    private String fromName;
    
    @Value("${app.base-url}")
    private String baseUrl;

    private Session session;
    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param toEmail adresse email du destinataire
     * @param newPassword nouveau mot de passe généré
     */
    public void envoyerEmailReinitialisationMotDePasse(String toEmail, String newPassword) {
        try {

            MimeMessage message = new MimeMessage(session);

            
            message.setFrom(fromEmail);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Confidentiel");
            
            String htmlContent = buildResetPasswordEmail(toEmail, newPassword);
            message.setText(htmlContent, "UTF-8");

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du mail", e);
        }
    }
    
    /**
     * Crée le contenu HTML du mail de réinitialisation
     * @param email adresse email du destinataire
     * @param password nouveau mot de passe
     * @return le contenu HTML du mail
     */
    private String buildResetPasswordEmail(String email, String password) {
        StringBuilder content = new StringBuilder();
        
        content.append("<!DOCTYPE html>");
        content.append("<html>");
        content.append("<head>");
        content.append("<link href='https://fonts.googleapis.com/css?family=Public+Sans' rel='stylesheet'>");
        content.append("</head>");                
        content.append("<body>");
        content.append("<img src=\"").append(baseUrl).append("/common/img/logo.png").append("\" width=\"253\" height=\"114\" alt=\"\">");
        content.append("<br><br>");
        content.append("<p style=\"font-family: 'Sans'\">Bonjour ");
        content.append(email).append("<br><br>");        
        content.append("&nbsp;&nbsp;Comme vous avez oubli&eacute; votre mot de passe praticboutic un nouveau a &eacute;t&eacute; g&eacute;n&eacute;r&eacute; automatiquement. <br>");        
        content.append("Voici votre nouveau mot de mot de passe administrateur praticboutic : ");
        content.append("<b>").append(password).append("</b><br>");
        content.append("Vous pourrez en personnaliser un nouveau à partir du formulaire client de l'arrière boutic.<br><br>");
        content.append("Cordialement<br><br>L'équipe praticboutic<br><br></p>");
        content.append("</body>");
        content.append("</html>");
        
        return content.toString();
    }
}