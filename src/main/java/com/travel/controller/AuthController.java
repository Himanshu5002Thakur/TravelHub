package com.travel.controller;

import com.travel.entity.User;
import com.travel.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@ModelAttribute User user, RedirectAttributes redirect) {

        try {
            userService.saveUser(user);
            redirect.addFlashAttribute("success", "Signup successful! Please login.");
            return "redirect:/login";

        } catch (RuntimeException e) {

            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }
}