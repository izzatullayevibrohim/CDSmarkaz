package org.example.cdsmarkaztelegrambot.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.cdsmarkaztelegrambot.models.User;
import org.example.cdsmarkaztelegrambot.repositories.UserRepository;
import org.example.cdsmarkaztelegrambot.util.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginPage() {
        System.out.println("keldiiiii");
        return "login";
    }

    @PostMapping("/login-check")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response) {

        User user = userRepository.findByTelegramUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login?error=user";
        }
        if (user.getPassword().equals(password)) {
            String token = jwtUtil.generateToken(username);
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24);
            response.addCookie(cookie);
            return "redirect:/admin-page";
        }
        return "redirect:/login?error=pass";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }
}
