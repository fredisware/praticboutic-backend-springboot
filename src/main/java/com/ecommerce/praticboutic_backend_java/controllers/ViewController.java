package com.ecommerce.praticboutic_backend_java.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @GetMapping("/autoclose")
    public String showAutoclosePage(@RequestParam String sessionid, Model model) {
        model.addAttribute("sessionid", sessionid);
        return "autoclose"; // => templates/autoclose.html
    }
}