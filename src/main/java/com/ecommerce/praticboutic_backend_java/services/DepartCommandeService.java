package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Commande;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.entities.LigneCmd;
import com.ecommerce.praticboutic_backend_java.entities.StatutCmd;
import com.ecommerce.praticboutic_backend_java.models.Item2;
import com.ecommerce.praticboutic_backend_java.repositories.CommandeRepository;
import com.ecommerce.praticboutic_backend_java.repositories.CustomerRepository;
import com.ecommerce.praticboutic_backend_java.repositories.LigneCmdRepository;
import com.ecommerce.praticboutic_backend_java.repositories.StatutCmdRepository;
import com.ecommerce.praticboutic_backend_java.requests.DepartCommandeRequest;
import com.ecommerce.praticboutic_backend_java.requests.Item;
import com.ecommerce.praticboutic_backend_java.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class DepartCommandeService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private LigneCmdRepository ligneCmdRepository;

    @Autowired
    private StatutCmdRepository statutCmdRepository;



    public void sendEmail(String recipientEmail, String subject, String compteurCommande, Map<String, Object> input , Double sum, HttpSession session) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        message.setFrom("onairmenu@gmail.com");
        helper.setTo(recipientEmail);
        helper.setSubject(subject);

        String strContent = generateEmailContent(compteurCommande, input, sum, session);

        helper.setText(strContent, true);

        mailSender.send(message);
    }


    public String generateEmailContent(String compteurCommande, Map<String, Object> input, Double sum, HttpSession session) {


        StringBuilder text = new StringBuilder();

        // Début du contenu HTML
        text.append("<!DOCTYPE html>");
        text.append("<html>");
        text.append("<head>");
        text.append("<link href='https://fonts.googleapis.com/css?family=Public+Sans' rel='stylesheet'>");
        text.append("</head>");
        text.append("<body>");

        // Logo SVG
        text.append("<svg version=\"1.2\" baseProfile=\"tiny\" id=\"Calque_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 295.7 51.8\" width=\"300\" height=\"51.057\" overflow=\"visible\" xml:space=\"preserve\">");
        text.append("<g>");
        text.append("<path fill=\"none\" d=\"M205.2,14.5c-3.2,0-5.2,2.5-5.2,5.5v0.1c0,3,2.1,5.6,5.2,5.6c3.2,0,5.2-2.5,5.2-5.5v-0.1C210.5,17.1,208.3,14.5,205.2,14.5z\"></path>");
        text.append("<path fill=\"none\" d=\"M183.1,14.4c-2.6,0-4.8,2.2-4.8,5.6v0.1c0,3.3,2.2,5.6,4.8,5.6c2.6,0,4.9-2.2,4.9-5.6V20C187.9,16.7,185.7,14.4,183.1,14.4z\"></path>");
        text.append("<path fill=\"none\" d=\"M87.5,14.4c-2.6,0-4.8,2.2-4.8,5.6v0.1c0,3.3,2.2,5.6,4.8,5.6c2.6,0,4.9-2.2,4.9-5.6V20C92.4,16.7,90.2,14.4,87.5,14.4z\"></path>");
        text.append("<path fill=\"none\" d=\"M124,22c-1-0.5-2.2-0.8-3.6-0.8c-2.4,0-3.9,1-3.9,2.8v0.1c0,1.5,1.3,2.4,3.1,2.4c2.6,0,4.4-1.5,4.4-3.5L124,22L124,22z\"></path>");
        // ... (reste du SVG)
        text.append("</g>");
        text.append("<polygon fill=\"none\" points=\"11.3,25.2 5.2,27.7 11.3,25.2 11.3,25.2 \"></polygon>");
        text.append("<polygon fill=\"none\" points=\"59.2,15.4 59.4,5.2 26.3,11.2 34,26.4 \"></polygon>");
        text.append("<path fill=\"#595959\" d=\"M11.8,13.9l9.6-1.7l9.1,18l0.6,8.8c0,0.8,0.5,1.5,1.1,1.9c0.4,0.2,0.8,0.4,1.2,0.4c0.3,0,0.6-0.1,0.9-0.2l28.8-12.6c1.2-0.5,1.7-1.9,1.2-3.1c-0.5-1.2-1.9-1.8-3.1-1.2L35.6,35.3L35.4,31l27.1-11.8c0.9-0.4,1.4-1.2,1.4-2.2l0.2-14.6c0-0.7-0.3-1.4-0.8-1.9c-0.5-0.5-1.2-0.7-1.9-0.5L11,9.2c-1.3,0.2-2.1,1.5-1.9,2.8C9.3,13.2,10.5,14.1,11.8,13.9z M59.4,5.2l-0.2,10.2L34,26.4l-7.7-15.2L59.4,5.2z\"></path>");
        text.append("<path fill=\"#E1007A\" d=\"M11.3,25.2C11.3,25.2,11.3,25.2,11.3,25.2L11.3,25.2l0.4-0.2c0.6-0.3,1.4,0.1,1.6,0.7c0.3,0.6-0.1,1.4-0.7,1.6l0,0L0.8,32.2c-0.6,0.3-0.9,1-0.7,1.6c0.3,0.6,1,0.9,1.6,0.7l13.1-5.3c0.6-0.3,1.4,0.1,1.6,0.7c0.3,0.6-0.1,1.4-0.7,1.6l-5.4,2.2l-2,0.8c-0.6,0.3-0.9,1-0.7,1.6c0.2,0.6,1,0.9,1.6,0.7l11.3-4.6c0,0,0,0,0,0c2.5-1,4.2-1.7,4.2-1.7c0.9-0.4,1.3-1.4,1-2.3l-3.3-8.2c-0.4-0.9-1.4-1.4-2.3-1c0,0-4.8,1.9-10.2,4.2l0,0l-9.2,3.7c-0.6,0.3-0.9,1-0.7,1.6c0.3,0.6,1,0.9,1.6,0.7l3.5-1.4L11.3,25.2z\"></path>");
        text.append("<path fill=\"#595959\" d=\"M57.3,35.8c-0.2-0.5-0.8-0.8-1.4-0.6l-34,14.6c-0.5,0.2-0.8,0.8-0.5,1.4c0.2,0.4,0.5,0.6,1,0.6c0.1,0,0.3,0,0.4-0.1l34-14.6C57.3,36.9,57.5,36.3,57.3,35.8z\"></path>");
        text.append("<path fill=\"#595959\" d=\"M55,40.4L39.7,47c-0.5,0.2-0.8,0.8-0.5,1.4c0.2,0.4,0.6,0.6,1,0.6c0.1,0,0.3,0,0.4-0.1l15.2-6.5c0.5-0.2,0.8-0.8,0.5-1.4S55.5,40.2,55,40.4z\"></path>");
        // ... (reste du SVG)
        text.append("</svg>");

        text.append("<br><br>");
        text.append("<p style=\"font-family: 'Sans'\"><b>Référence commande: </b> ").append(compteurCommande).append("<br></p>");
        text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");

        if (Integer.parseInt((String) session.getAttribute("method")) == 2) {
            text.append("<p style=\"font-family: 'Sans'\"><b>Vente : </b>Consomation sur place<br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            text.append("<p style=\"font-family: 'Sans'\"><b>Commande table numéro : </b> ").append((String) session.getAttribute("table")).append("<br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            text.append("<p style=\"font-family: 'Sans'\"><b>Téléphone : </b>").append(input.get("telephone")).append("<br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
        }

        if (Integer.parseInt((String) session.getAttribute("method")) == 3) {
            if (input.get("vente").equals("EMPORTER")) {
                text.append("<p style=\"font-family: 'Sans'\"><b>Vente : </b> A emporter<br></p>");
                text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            }
            if (input.get("vente").equals("LIVRER")) {
                text.append("<p style=\"font-family: 'Sans'\"><b>Vente : </b> A livrer<br></p>");
                text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            }
            if (input.get("paiement").equals("COMPTANT")) {
                text.append("<p style=\"font-family: 'Sans'\"><b>Paiement : </b> Au comptant<br></p>");
                text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            }
            if (input.get("paiement").equals("LIVRAISON")) {
                text.append("<p style=\"font-family: 'Sans'\"><b>Paiement : </b> A la livraison<br></p>");
                text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            }

            text.append("<p style=\"font-family: 'Sans'\"><b>Nom du client : </b>")
                    .append(Utils.sanitizeInput(input.get("nom").toString())).append(" ")
                    .append(Utils.sanitizeInput(input.get("prenom").toString()))
                    .append("<br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            text.append("<p style=\"font-family: 'Sans'\"><b>Téléphone : </b>").append(input.get("telephone").toString()).append("<br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");

            if (input.get("vente").equals("LIVRER")) {
                text.append("<p style=\"font-family: 'Sans'\"><b>Adresse (ligne1) : </b>")
                        .append(Utils.sanitizeInput(input.get("adresse1").toString()))
                        .append("</p><hr style=\"width:50%;text-align:left;margin-left:0\">");
                text.append("<p style=\"font-family: 'Sans'\"><b>Adresse (ligne2) : </b>")
                        .append(Utils.sanitizeInput(input.get("adresse2").toString()))
                        .append("</p><hr style=\"width:50%;text-align:left;margin-left:0\">");
                text.append("<p style=\"font-family: 'Sans'\"><b>Code Postal : </b>")
                        .append(Utils.sanitizeInput(input.get("codepostal").toString()))
                        .append("<br><hr style=\"width:50%;text-align:left;margin-left:0\"></p>");
                text.append("<p style=\"font-family: 'Sans'\"><b>Ville : </b>")
                        .append(Utils.sanitizeInput(input.get("ville").toString()))
                        .append("<br><hr style=\"width:50%;text-align:left;margin-left:0\"></p>");
            }
        }

        text.append("<p style=\"font-family: 'Sans'\"><b>Information complémentaire : </b>");
        // Conversion nl2br et stripslashes
        String infoSup = input.get("infosup").toString()
                .replace("\n", "<br>")
                .replace("\\", "");
        infoSup = Utils.sanitizeInput(infoSup);
        text.append(infoSup).append("</p>");
        text.append("<hr style=\"border: 3px solid black;margin-top:15px;margin-bottom:25px;width:50%;text-align:left;margin-left:0\">");

        Integer val = 0;

        text.append("<p style=\"font-size:130%;margin-bottom:25px;font-family: 'Sans'\"><b>Détail de la commande : </b><br></p>");

        //ArrayList<Item> items = (ArrayList<Item>) input.get("items");
        List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) input.get("items");

        ObjectMapper mapper = new ObjectMapper();
        List<Item> items = itemsRaw.stream()
                .map(map -> mapper.convertValue(map, Item.class))
                .collect(Collectors.toList());
        int numItems = items.size();
        int i = 0;

        for (Item item : items) {
            i++;

            text.append("<p style=\"font-family: 'Sans'\">");
            text.append("Ligne ").append(i).append("<br>");
            text.append("<b>").append(Utils.sanitizeInput(item.getName())).append("</b><br>");

            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            text.append(item.getQt())
                    .append(" x ")
                    .append(formatter.format(item.getPrix()))
                    .append(Utils.sanitizeInput(item.getUnite()))
                    .append("<br>");

            text.append(item.getOpts());

            // Conversion nl2br et stripslashes pour txta
            String txta = item.getTxta()
                    .replace("\n", "<br>")
                    .replace("\\", "");
            txta = Utils.sanitizeInput(txta);

            text.append("<i>").append(txta).append("</i>");
            text.append("</p>");

            if (i != numItems) {
                text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            }

            val += item.getQt();
            sum += item.getPrix() * item.getQt();
        }

        text.append("<hr style=\"border: 3px solid black;margin-top:15px;margin-bottom:25px;width:50%;text-align:left;margin-left:0\">");

        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        if (!input.get("vente").equals("LIVRER")) {
            text.append("<p style=\"font-size:130%;font-family: 'Sans'\">Remise : ")
                    .append(formatter.format(-Double.parseDouble(input.get("remise").toString())))
                    .append("€ <br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            text.append("<p style=\"font-size:130%;font-family: 'Sans'\"><b>Total Commande : ")
                    .append(formatter.format(sum - Double.parseDouble(input.get("remise").toString())))
                    .append("€ </b><br></p>");
        } else {
            text.append("<p style=\"font-size:130%;font-family: 'Sans'\">Sous-total Commande : ")
                    .append(formatter.format(sum))
                    .append("€ <br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            text.append("<p style=\"font-size:130%;font-family: 'Sans'\">Remise : ")
                    .append(formatter.format(-Double.parseDouble(input.get("remise").toString())))
                    .append("€ <br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            text.append("<p style=\"font-size:130%;font-family: 'Sans'\">Frais de Livraison : ")
                    .append(formatter.format(Double.parseDouble(input.get("fraislivr").toString())))
                    .append("€ <br></p>");
            text.append("<hr style=\"width:50%;text-align:left;margin-left:0\">");
            text.append("<p style=\"font-size:130%;font-family: 'Sans'\"><b>Total Commande : ")
                    .append(formatter.format(sum - Double.parseDouble(input.get("remise").toString()) + Double.parseDouble(input.get("fraislivr").toString())))
                    .append("€ </b><br></p>");
        }

        text.append("</body>");
        text.append("</html>");

        return text.toString();

    }

    public Integer enregistreCommande(String compteurCommande, Map<String, Object> input, Double sum, HttpSession session)
    {
        // Enregistrer la commande dans la base de données
        Customer custo = customerRepository.findByCustomer((String) session.getAttribute("customer"));

        Commande order = new Commande();
        order.setNumRef(compteurCommande);
        order.setCustomId(custo.getCustomId());

        // Récupérer les données du Map input
        // Assurez-vous que les clés correspondent à vos données d'entrée
        order.setNom((String) input.get("nom"));
        order.setPrenom((String) input.get("prenom"));
        order.setTelephone((String) input.get("telephone"));
        order.setAdresse1((String) input.get("adresse1"));
        order.setAdresse2((String) input.get("adresse2"));
        order.setCodePostal((String) input.get("codepostal"));
        order.setVille((String) input.get("ville"));
        order.setVente((String) input.get("vente"));
        order.setPaiement((String) input.get("paiement"));
        order.setSsTotal(sum);
        order.setRemise(-(Double) input.get("remise"));
        order.setFraisLivraison((Double) input.get("fraislivr"));
        order.setTotal(sum - (Double) input.get("remise") + (Double) input.get("fraislivr"));
        order.setCommentaire((String) input.get("infosup"));

        // Convertir la méthode de commande en chaîne de caractères
        String methodStr = switch ((String)session.getAttribute("method")) {
            case "2" -> "ATABLE";
            case "3" -> "CLICKNCOLLECT";
            default -> "INCONNU";
        };
        order.setMethod(methodStr);

        // Convertir la valeur de la table en Integer
        String tableValue = (String) session.getAttribute("table");
        if (tableValue != null && !tableValue.isEmpty()) {
            order.setTable(Integer.parseInt(tableValue));
        }

        order.setDateCreation(LocalDateTime.now());
        order.setStatId(statutCmdRepository.findByCustomidAndDefaut(custo.getCustomId(), 1).getStatid());

        // Sauvegarde de la commande
        commandeRepository.save(order);

        Integer cmdId = order.getCmdId();
        if (cmdId == null || cmdId == 0) {
            throw new RuntimeException("Erreur lors de la récupération de l'ID de la commande");
        }

        // Enregistrer les lignes de commande
        Integer ordre = 0;
        @SuppressWarnings("unchecked")
        //List<Item> items = (List<Item>) input.get("items");
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) input.get("items");
        List<Item> items = rawItems.stream()
                .map(map -> mapper.convertValue(map, Item.class))
                .collect(Collectors.toList());
        if (items != null) {
            for (Item item : items) {
                ordre++;
                enregistreLigneCmd(custo.getCustomId(), ordre, cmdId, item);
            }
        }

        return cmdId;
    }

    public void enregistreLigneCmd(Integer customId, Integer ordre, Integer cmdId, Item item)
    {
        // Initialisation des IDs pour l'article et l'option
        Integer artId = 0;
        Integer optId = 0;

        // Détermination de l'ID en fonction du type de l'item
        if ("article".equals(item.getType())) {
            artId = Integer.parseInt(item.getId());
        } else if ("option".equals(item.getType())) {
            optId = Integer.parseInt(item.getId());
        }

        // Création d'une nouvelle instance de LigneCmd
        LigneCmd ligneCmd = new LigneCmd();
        ligneCmd.setCustomId(customId); // customid doit être défini dans le contexte de la classe
        ligneCmd.setCmdId(cmdId);
        ligneCmd.setOrdre(ordre);
        ligneCmd.setType(item.getType());
        ligneCmd.setNom(item.getName());
        ligneCmd.setPrix(item.getPrix().floatValue()); // Conversion de Double à Float
        ligneCmd.setQuantite(item.getQt().floatValue()); // Conversion de Integer à Float
        ligneCmd.setCommentaire(item.getTxta());
        ligneCmd.setArtId(artId);
        ligneCmd.setOptId(optId);

        // Enregistrement de la ligne de commande dans la base de données
        // Ici, vous devez utiliser votre mécanisme de persistance (par exemple, un repository JPA)
        // Exemple avec un repository JPA :
         ligneCmdRepository.save(ligneCmd);
    }

}
