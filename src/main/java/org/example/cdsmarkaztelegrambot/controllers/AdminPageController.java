package org.example.cdsmarkaztelegrambot.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.cdsmarkaztelegrambot.models.CheckMessage;
import org.example.cdsmarkaztelegrambot.models.Role;
import org.example.cdsmarkaztelegrambot.models.User;
import org.example.cdsmarkaztelegrambot.repositories.RoleRepository;
import org.example.cdsmarkaztelegrambot.repositories.ScheduledFeedbackRepository;
import org.example.cdsmarkaztelegrambot.repositories.UserRepository;
import org.example.cdsmarkaztelegrambot.services.FeedBackService;
import org.example.cdsmarkaztelegrambot.services.MediaFileService;
import org.example.cdsmarkaztelegrambot.services.WelcomeMessageService;
import org.example.cdsmarkaztelegrambot.telegramBot.handler.MessageHandler;
import org.example.cdsmarkaztelegrambot.util.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminPageController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final MediaFileService mediaFileService;
    private final WelcomeMessageService welcomeMessageService;
    private final MessageHandler messageHandler;
    private final ScheduledFeedbackRepository scheduledFeedbackRepository;
    private final FeedBackService feedBackService;

    @GetMapping("/login")
    public String loginPage() {
        System.out.println("keldiiiii");
        return "login";
    }

    @PostMapping("/login-check")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response) {

        User user = userRepository.findByTelegramUsername(username);
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

    @GetMapping("/admin-page")
    public String adminPage(HttpServletRequest request, Model model) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("jwt")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null)  return "redirect:/login";
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByTelegramUsername(username);
        if (user == null) return "redirect:/login";
        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        List<User> users = new ArrayList<>(userRepository.findByRoleIdIsNull());
        Long userCount = userRepository.count();
        Long countIsActive = userRepository.countByIsActive(Boolean.TRUE);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Long todayUsers = userRepository.countByCreatedAtBetween(start, end);
        model.addAttribute("todayUsers", todayUsers);
        model.addAttribute("countIsActive", countIsActive);
        model.addAttribute("userCount", userCount);
        model.addAttribute("users", users);
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        return "admin-page";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

    @PostMapping(value = "/send-welcome", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String sendWelcome(@RequestParam String type,
                              @RequestParam String message,
                              @RequestParam(required = false) String caption,
                              @RequestParam MultipartFile file) throws IOException {
       if (file != null) {
           Long mediaFileId = mediaFileService.uploadFile(file, type);
           if (mediaFileId != null) {
               welcomeMessageService.saveWelcomeMessage(type, message, caption, mediaFileId);
               return "redirect:/admin-page";
           }
       }
       welcomeMessageService.saveOnlyMessage(message);
        return "redirect:/admin-page";
    }

    @PostMapping(value = "/send-check", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String sendCheck(@RequestParam String type,
                            @RequestParam(required = false) String message,
                            @RequestParam(required = false) String caption,
                            @RequestParam(required = false) MultipartFile file,
                            @RequestParam Integer delayMinutes) throws IOException {

        Long mediaFileId = null;
        if (file != null && !file.isEmpty()) {
            mediaFileId = mediaFileService.uploadFile(file, type);
        }

        CheckMessage checkMessage = CheckMessage.builder()
                .type(type)
                .message(message)
                .caption(caption)
                .mediaFileId(mediaFileId)
                .delayMinutes(delayMinutes)
                .build();

        messageHandler.broadcastAndSchedule(checkMessage);

        return "redirect:/admin-page";
    }

    @PostMapping("/set-feed-back-text")
    public String setFeedBackMessage(@RequestParam String message){
        feedBackService.saveFeedbackMessage(message);
        return "redirect:/admin-page";
    }
}