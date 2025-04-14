package com.ecommerce.praticboutic_backend_java.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class RedirectController {

    @PostMapping("/redirect")
    public void handleRedirect(
            @RequestParam(value = "sessionid", required = false, defaultValue = "") String sessionId,
            @RequestParam(value = "platform", required = false, defaultValue = "web") String platform,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        // Déterminer le protocole (http ou https)
        String protocol = request.isSecure() ? "https://" : "http://";

        // Construire le nom du serveur avec le port
        String server = request.getServerName() + ":" + request.getServerPort();

        // Redirection en fonction de la plateforme
        if ("android".equals(platform)) {
            // Redirection vers l'application mobile
            response.sendRedirect("praticboutic://onboarding-complete?sessionid=" + sessionId);
        } else {
            // Redirection vers la version web
            response.sendRedirect("redirect:/autoclose?sessionid=" + sessionId);
        }
    }

    @PostMapping("/autoclose")
    public ModelAndView autoClose(@RequestParam(required = false) String sessionid) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("autoclose");
        modelAndView.addObject("sessionid", sessionid);
        return modelAndView;
    }
}
