package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.configurations.StripeConfig;

import com.ecommerce.praticboutic_backend_java.services.ParameterService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.SubscriptionCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FrontController {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private StripeConfig stripeConfig;
    
    @Autowired
    private ParameterService paramService;

    @PostMapping("/front")
    public ResponseEntity<?> handleRequest(@RequestBody FrontRequest request, HttpSession session) {
        try {
            // Vérifier la session
            if (request.getSessionid() != null) {
                session.getId();
            }
            
            // Durée de vie maximale de la session en secondes
            int maxLifetime = session.getMaxInactiveInterval();
            
            // Vérifie si la session est active
            Long lastActivity = (Long) session.getAttribute("last_activity");
            if (lastActivity != null) {
                // Vérifie si le temps écoulé depuis la dernière activité dépasse la durée de vie maximale
                if ((System.currentTimeMillis() / 1000) - lastActivity > maxLifetime) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Session expirée"));
                } else {
                    // La session est toujours active, met à jour le timestamp de la dernière activité
                    session.setAttribute("last_activity", System.currentTimeMillis() / 1000);
                }
            } else {
                // Si last_activity n'est pas défini, la session est considérée comme expirée
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Session expirée"));
            }
            
            // Traiter différents types de requêtes
            String requete = request.getRequete();
            if (requete == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Type de requête non spécifié"));
            }
            
            List<Object> result;
            
            switch (requete) {
                case "categories":
                    result = getCategories(request.getBouticid());
                    break;
                case "articles":
                    result = getArticles(request.getBouticid(), request.getCatid());
                    break;
                case "groupesoptions":
                    result = getGroupesOptions(request.getBouticid(), request.getArtid());
                    break;
                case "options":
                    result = getOptions(request.getBouticid(), request.getGrpoptid());
                    break;
                case "getBouticInfo":
                    result = getBouticInfo(request.getCustomer());
                    break;
                case "getClientInfo":
                    result = getClientInfo(request.getCustomer());
                    break;
                case "images":
                    result = getImages(request.getBouticid(), request.getArtid());
                    break;
                case "aboactif":
                    result = getActiveSubscriptions(request.getBouticid());
                    break;
                case "initSession":
                    result = initSession(request.getCustomer(), request.getMethod(), request.getTable(), session);
                    break;
                case "getSession":
                    result = getSession(session);
                    break;
                case "getparam":
                    result = getParam(request.getParam(), request.getBouticid());
                    break;
                default:
                    return ResponseEntity.badRequest().body(new ErrorResponse("Type de requête non reconnu"));
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    private List<Object> getCategories(int bouticId) throws SQLException {
        List<Object> categories = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT catid, nom, visible FROM categorie WHERE customid = ? OR catid = 0 ORDER BY catid";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bouticId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<Object> category = new ArrayList<>();
                        category.add(rs.getInt("catid"));
                        category.add(rs.getString("nom"));
                        category.add(rs.getInt("visible"));
                        categories.add(category);
                    }
                }
            }
        }
        
        return categories;
    }
    
    private List<Object> getArticles(int bouticId, int catId) throws SQLException {
        List<Object> articles = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT artid, nom, prix, unite, description, image FROM article " +
                           "WHERE customid = ? AND visible = 1 AND catid = ? ORDER BY artid";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bouticId);
                stmt.setInt(2, catId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<Object> article = new ArrayList<>();
                        article.add(rs.getInt("artid"));
                        article.add(rs.getString("nom"));
                        article.add(rs.getDouble("prix"));
                        article.add(rs.getString("unite"));
                        article.add(rs.getString("description"));
                        article.add(rs.getString("image"));
                        articles.add(article);
                    }
                }
            }
        }
        
        return articles;
    }
    
    private List<Object> getGroupesOptions(int bouticId, int artId) throws SQLException {
        List<Object> groupesOptions = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT groupeopt.grpoptid, groupeopt.nom, groupeopt.multiple " +
                           "FROM relgrpoptart, groupeopt " +
                           "WHERE relgrpoptart.customid = ? AND groupeopt.customid = ? " +
                           "AND relgrpoptart.visible = 1 AND groupeopt.visible = 1 " +
                           "AND artid = ? AND relgrpoptart.grpoptid = groupeopt.grpoptid " +
                           "ORDER BY groupeopt.grpoptid";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bouticId);
                stmt.setInt(2, bouticId);
                stmt.setInt(3, artId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<Object> groupeOption = new ArrayList<>();
                        groupeOption.add(rs.getInt("grpoptid"));
                        groupeOption.add(rs.getString("nom"));
                        groupeOption.add(rs.getInt("multiple"));
                        groupesOptions.add(groupeOption);
                    }
                }
            }
        }
        
        return groupesOptions;
    }
    
    private List<Object> getOptions(int bouticId, int grpoptId) throws SQLException {
        List<Object> options = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT optid, nom, surcout FROM `option` " +
                           "WHERE customid = ? AND visible = 1 AND grpoptid = ? ORDER BY optid";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bouticId);
                stmt.setInt(2, grpoptId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<Object> option = new ArrayList<>();
                        option.add(rs.getInt("optid"));
                        option.add(rs.getString("nom"));
                        option.add(rs.getDouble("surcout"));
                        options.add(option);
                    }
                }
            }
        }
        
        return options;
    }
    
    private List<Object> getBouticInfo(String customer) throws SQLException {
        List<Object> bouticInfo = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT customid, logo, nom FROM customer WHERE customer = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, customer);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        bouticInfo.add(rs.getInt("customid"));
                        bouticInfo.add(rs.getString("logo"));
                        bouticInfo.add(rs.getString("nom"));
                    }
                }
            }
        }
        
        return bouticInfo;
    }
    
    private List<Object> getClientInfo(String customer) throws SQLException {
        List<Object> clientInfo = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT CU.customid, CU.nom, CL.adr1, CL.adr2, CL.cp, CL.ville, CU.logo " +
                           "FROM customer CU, client CL " +
                           "WHERE CU.customer = ? AND CL.cltid = CU.cltid LIMIT 1";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, customer);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int customid = rs.getInt("customid");
                        String nom = rs.getString("nom");
                        String adresse1 = rs.getString("adr1");
                        String adresse2 = rs.getString("adr2");
                        String codePostal = rs.getString("cp");
                        String ville = rs.getString("ville");
                        String logo = rs.getString("logo");
                        
                        String adr = nom + " " + adresse1 + " " + adresse2 + " " + codePostal + " " + ville;
                        
                        clientInfo.add(customid);
                        clientInfo.add(nom);
                        clientInfo.add(adr);
                        clientInfo.add(logo);
                    }
                }
            }
        }
        
        return clientInfo;
    }
    
    private List<Object> getImages(int bouticId, int artId) throws SQLException {
        List<Object> images = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT image FROM artlistimg " +
                           "WHERE customid = ? AND visible = 1 AND artid = ? " +
                           "ORDER BY favori DESC, artlistimgid ASC";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bouticId);
                stmt.setInt(2, artId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<Object> image = new ArrayList<>();
                        image.add(rs.getString("image"));
                        images.add(image);
                    }
                }
            }
        }
        
        return images;
    }
    
    private List<Object> getActiveSubscriptions(int bouticId) throws SQLException, StripeException {
        String stripeCustomerId;
        
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT client.stripe_customer_id FROM abonnement, client " +
                           "WHERE abonnement.bouticid = ? AND abonnement.cltid = client.cltid LIMIT 1";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bouticId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next() || rs.getString("stripe_customer_id").isEmpty()) {
                        throw new RuntimeException("Impossible de récupérer l'identifiant Stripe de la boutic");
                    }
                    
                    stripeCustomerId = rs.getString("stripe_customer_id");
                }
            }
        }
        
        // Configurer Stripe
        Stripe.apiKey = stripeConfig.getSecretKey();
        
        // Récupérer les abonnements actifs
        Map<String, Object> params = new HashMap<>();
        params.put("customer", stripeCustomerId);
        params.put("status", "active");
        
        SubscriptionCollection subscriptions = com.stripe.model.Subscription.list(params);
        
        return List.of(subscriptions.getData());
    }
    
    private List<Object> initSession(String customer, String method, String table, HttpSession session) {
        session.setAttribute("customer", customer);
        session.setAttribute(customer + "_mail", "non");
        session.setAttribute("method", method != null ? method : "3");
        session.setAttribute("table", table != null ? table : "0");
        
        List<Object> sessionInfo = new ArrayList<>();
        sessionInfo.add(session.getId());
        
        return sessionInfo;
    }
    
    private List<Object> getSession(HttpSession session) {
        List<Object> sessionInfo = new ArrayList<>();
        
        String customer = (String) session.getAttribute("customer");
        String mail = (String) session.getAttribute(customer + "_mail");
        String method = (String) session.getAttribute("method");
        String table = (String) session.getAttribute("table");
        
        sessionInfo.add(customer);
        sessionInfo.add(mail);
        sessionInfo.add(method);
        sessionInfo.add(table);
        
        return sessionInfo;
    }
    
    private List<Object> getParam(String param, int bouticId) throws SQLException {
        List<Object> paramValue = new ArrayList<>();
        String value = paramService.getValeurParam(param, bouticId, "");
        paramValue.add(value);
        return paramValue;
    }
    
    // Classes pour la sérialisation/désérialisation JSON
    public static class FrontRequest {
        private String sessionid;
        private String requete;
        private int bouticid;
        private int catid;
        private int artid;
        private int grpoptid;
        private String customer;
        private String method;
        private String table;
        private String param;

        public String getSessionid() {
            return sessionid;
        }

        public void setSessionid(String sessionid) {
            this.sessionid = sessionid;
        }

        public String getRequete() {
            return requete;
        }

        public void setRequete(String requete) {
            this.requete = requete;
        }

        public int getBouticid() {
            return bouticid;
        }

        public void setBouticid(int bouticid) {
            this.bouticid = bouticid;
        }

        public int getCatid() {
            return catid;
        }

        public void setCatid(int catid) {
            this.catid = catid;
        }

        public int getArtid() {
            return artid;
        }

        public void setArtid(int artid) {
            this.artid = artid;
        }

        public int getGrpoptid() {
            return grpoptid;
        }

        public void setGrpoptid(int grpoptid) {
            this.grpoptid = grpoptid;
        }

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }
    
    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}