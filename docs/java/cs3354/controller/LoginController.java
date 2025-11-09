package com.cs3354Team3.cs3354GroupProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // refers to login.html in /templates
    }

    @GetMapping("/default")
    public String redirectAfterLogin(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority();

        switch (role) {
            case "ADMIN":
                return "redirect:/admin/dashboard";
            case "TEACHER":
                return "redirect:/teacher/dashboard";
            case "STUDENT":
                return "redirect:/student/dashboard";
            default:
                return "redirect:/login";
        }
    }
}
