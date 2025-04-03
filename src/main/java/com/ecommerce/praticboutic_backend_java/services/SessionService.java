package com.ecommerce.praticboutic_backend_java.services;

import com.google.api.client.util.Value;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.UUID;


@Service
public class SessionService {

    private JdbcTemplate jdbcTemplate;




    public void setSessionId(String sessionId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        request.changeSessionId(); // Méthode pour changer l'ID de session (peut varier selon la version de Spring)
    }

    public boolean hasAttribute(String name) {
        HttpSession session = getSession();
        return session.getAttribute(name) != null;
    }

    public Object getAttribute(String name) {
        HttpSession session = getSession();
        return session.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        HttpSession session = getSession();
        session.setAttribute(name, value);
    }

    private HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true);
    }

    public boolean isSessionValid(String sessionId) {
        // Implémentation pour vérifier la validité de la session
        return true; // À implémenter selon votre logique de gestion de session
    }

    public boolean isAuthenticated() {
        HttpSession session = getSession();
        return session.getAttribute("bo_auth") != null &&
                session.getAttribute("bo_auth").equals("oui");
    }

    public String getUserEmail() {
        HttpSession session = getSession();
        return (String) session.getAttribute("bo_email");
    }

    public void setBoId(Integer bouticId) {
        HttpSession session = getSession();
        session.setAttribute("bo_id", bouticId);
    }

    public void getBoId() {
        HttpSession session = getSession();
        session.getAttribute("bo_id");
    }


    public void updateSession(Map<String, Object> sessionData) {
        //HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        //request.changeSessionId(); // Méthode pour changer l'ID de session (peut varier selon la version de Spring)
        //setSessionId(sessionId);
        //sessionId = request.getSession().getId();

        for (Map.Entry<String, Object> entry : sessionData.entrySet()) {
            getSession().setAttribute(entry.getKey(), entry.getValue());
        }

    }
}