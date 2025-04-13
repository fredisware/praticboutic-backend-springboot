package com.ecommerce.praticboutic_backend_java.services;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

@Service
public class SmsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${sms.sender}")
    private String smsSender;

    @Value("${sms.token}")
    private String smsToken;

    /**
     * Envoie un SMS avec les détails de la commande si validSms est à 1
     *
     * @param validSms Indicateur si l'envoi SMS est activé
     * @param cmdId ID de la commande
     * @param customId ID du client/boutique
     * @param telMobile Numéro de téléphone mobile du destinataire
     * @return boolean indiquant si le SMS a été envoyé avec succès
     * @throws Exception En cas d'erreur durant le processus
     */
    public boolean sendOrderSms(String validSms, int cmdId, int customId, String telMobile) throws Exception {
        // Vérifier si l'envoi de SMS est activé
        if (!"1".equals(validSms)) {
            return false;
        }

        // Construction de la requête SQL

        String query = "SELECT commande.telephone, statutcmd.message, commande.numref, commande.nom, commande.prenom, " +
                "commande.adresse1, commande.adresse2, commande.codepostal, commande.ville, commande.vente, " +
                "commande.paiement, commande.sstotal, commande.fraislivraison, commande.total, " +
                "commande.commentaire, statutcmd.etat, customer.nom FROM commande " +
                "INNER JOIN statutcmd ON commande.statid = statutcmd.statid " +
                "INNER JOIN customer ON commande.customid = statutcmd.customid " +
                "WHERE statutcmd.defaut = 1 AND commande.cmdid = ? AND commande.customid = ? " +
                "AND statutcmd.customid = ? AND customer.customid = ? " +
                "ORDER BY commande.cmdid LIMIT 1";
        String message = "";

        try {
            // Création du formateur pour les nombres
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator(' ');
            DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);

            // Exécution de la requête
            Map<String, Object> result = jdbcTemplate.queryForMap(
                    query,
                    cmdId,
                    customId,
                    customId,
                    customId
            );

            String content = (String) result.get("message");

            // Remplacements des valeurs dans le contenu du message
            content = content.replace("%boutic%", (String) result.get("customer.nom"));
            content = content.replace("%telephone%", (String) result.get("commande.telephone"));
            content = content.replace("%numref%", (String) result.get("commande.numref"));
            content = content.replace("%nom%", (String) result.get("commande.nom"));
            content = content.replace("%prenom%", (String) result.get("commande.prenom"));
            content = content.replace("%adresse1%", (String) result.get("commande.adresse1"));
            content = content.replace("%adresse2%", (String) result.get("commande.adresse2"));
            content = content.replace("%codepostal%", (String) result.get("commande.codepostal"));
            content = content.replace("%ville%", (String) result.get("commande.ville"));
            content = content.replace("%vente%", (String) result.get("commande.vente"));
            content = content.replace("%paiement%", (String) result.get("commande.paiement"));

            // Formatage des valeurs numériques
            Double ssTotal = ((Number) result.get("commande.sstotal")).doubleValue();
            Double fraisLivraison = ((Number) result.get("commande.fraislivraison")).doubleValue();
            Double total = ((Number) result.get("commande.total")).doubleValue();

            content = content.replace("%sstotal%", formatter.format(ssTotal));
            content = content.replace("%fraislivraison%", formatter.format(fraisLivraison));
            content = content.replace("%total%", formatter.format(total));

            content = content.replace("%commentaire%", (String) result.get("commande.commentaire"));
            content = content.replace("%etat%", (String) result.get("statutcmd.etat"));

            message = content;
        } catch (DataAccessException e) {
            throw new Exception("Erreur lors de la récupération des données : " + e.getMessage());
        }

        // Si aucun message n'a été construit, on s'arrête là
        if (message.isEmpty()) {
            return false;
        }

        // Envoi du SMS via l'API SMSFactor
        return sendSmsViaApi(message, telMobile );
    }

    /**
     * Envoie un SMS via l'API SMSFactor
     *
     * @param messageText Contenu du message à envoyer
     * @param phoneNumber Numéro de téléphone du destinataire
     * @return boolean indiquant si l'envoi a réussi
     * @throws Exception En cas d'erreur durant l'envoi
     */
    public boolean sendSmsViaApi(String messageText, String phoneNumber) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);

            // Construction du message
            ObjectNode messageNode = mapper.createObjectNode();
            messageNode.put("text", messageText);
            messageNode.put("sender", smsSender);

            // Construction du destinataire
            ObjectNode recipientNode = mapper.createObjectNode();
            recipientNode.put("value", phoneNumber);

            ArrayNode recipientsArray = mapper.createArrayNode();
            recipientsArray.add(recipientNode);

            ObjectNode gsmNode = mapper.createObjectNode();
            gsmNode.set("gsm", recipientsArray);

            // Construction de l'objet SMS complet
            ObjectNode smsNode = mapper.createObjectNode();
            smsNode.set("message", messageNode);
            smsNode.set("recipients", gsmNode);

            // Construction de la payload finale
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.set("sms", smsNode);

            String payload = mapper.writeValueAsString(rootNode);

            // Envoi de la requête HTTP
            String response = post("https://api.smsfactor.com/send", payload);

            // Analyse de la réponse pour déterminer si l'envoi a réussi
            // Cette partie peut être adaptée selon le format de réponse exact de SMSFactor
            return response != null && response.contains("\"status\":\"success\"");

        } catch (Exception e) {
            throw new Exception("Erreur lors de l'envoi du SMS : " + e.getMessage());
        }
    }

    /**
     * Effectue une requête HTTP POST
     *
     * @param postUrl URL de destination
     * @param data Données à envoyer (JSON)
     * @return String contenant la réponse du serveur
     * @throws IOException En cas d'erreur réseau
     */
    public String post(String postUrl, String data) throws IOException {
        URL url = new URL(postUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + smsToken);
        con.setDoOutput(true);

        sendData(con, data);

        return read(con.getInputStream());
    }

    /**
     * Envoie les données à la connexion HTTP
     *
     * @param con Connexion HTTP
     * @param data Données à envoyer
     * @throws IOException En cas d'erreur d'envoi
     */
    protected void sendData(HttpURLConnection con, String data) throws IOException {
        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
        } catch (IOException exception) {
            throw exception;
        } finally {
            closeQuietly(wr);
        }
    }

    /**
     * Lit la réponse du serveur
     *
     * @param is Flux d'entrée
     * @return String contenant la réponse
     * @throws IOException En cas d'erreur de lecture
     */
    private String read(InputStream is) throws IOException {
        BufferedReader in = null;
        String inputLine;
        StringBuilder body;
        try {
            in = new BufferedReader(new InputStreamReader(is));
            body = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                body.append(inputLine);
            }
            return body.toString();
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            closeQuietly(in);
        }
    }

    /**
     * Ferme silencieusement une ressource
     *
     * @param closeable Ressource à fermer
     */
    protected void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {
            // Ignorer l'exception
        }
    }
}