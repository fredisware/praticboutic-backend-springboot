package com.ecommerce.praticboutic_backend_java.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api")
public class RedirectController {

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/redirect-handler")
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
            //response.sendRedirect(protocol + server + "/autoclose?sessionid=" + sessionId);
            String redirectUrl = baseUrl + "/autoclose?sessionid=" + sessionId;
            response.sendRedirect(redirectUrl);
        }
    }

    /*@PostMapping("/autoclose")
    public ModelAndView autoClose(@RequestParam(required = false) String sessionid) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("autoclose");
        modelAndView.addObject("sessionid", sessionid);
        return modelAndView;
    }*/

    @GetMapping("/autoclose")
    public String showAutoclosePage(@RequestParam String sessionid, Model model) {
        model.addAttribute("sessionid", sessionid);
        return "autoclose"; // => templates/autoclose.html
    }
}
