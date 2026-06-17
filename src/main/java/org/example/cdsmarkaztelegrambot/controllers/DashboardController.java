package org.example.cdsmarkaztelegrambot.controllers;

import lombok.RequiredArgsConstructor;
import org.example.cdsmarkaztelegrambot.models.CheckMessage;
import org.example.cdsmarkaztelegrambot.models.Role;
import org.example.cdsmarkaztelegrambot.models.User;
import org.example.cdsmarkaztelegrambot.repositories.RoleRepository;
import org.example.cdsmarkaztelegrambot.repositories.UserRepository;
import org.example.cdsmarkaztelegrambot.services.FeedBackService;
import org.example.cdsmarkaztelegrambot.services.MediaFileService;
import org.example.cdsmarkaztelegrambot.services.WelcomeMessageService;
import org.example.cdsmarkaztelegrambot.telegramBot.handler.MessageHandler;
import org.example.cdsmarkaztelegrambot.util.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final MediaFileService mediaFileService;
    private final WelcomeMessageService welcomeMessageService;
    private final MessageHandler messageHandler;
    private final FeedBackService feedBackService;


    @GetMapping("/admin-page")
    public String adminPage(Model model) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName();
        User user = userRepository.findByTelegramUsername(username).orElse(null);
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

    @PostMapping("/toggle-status")
    @ResponseBody
    public ResponseEntity<String> toggleStatus(@RequestParam String username) {
        User user = userRepository.findByTelegramUsername(username).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/get/user-data")
    public String getUserData(Model model, Long userId) {
        model.addAttribute("user", userRepository.findById(userId));
        return "admin-page";
    }

    @GetMapping("/profile")
    public String profile(@CookieValue(value = "jwt", required = false) String token,
                          Model model) {
        if (token == null) {
            return "redirect:/login";
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByTelegramUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@CookieValue(value = "jwt", required = false) String token,
                                @RequestParam String fullName,
                                @RequestParam String phoneNumber,
                                @RequestParam(required = false) String password) {
        if (token == null) {
            return "redirect:/login";
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByTelegramUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);

        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }

        userRepository.save(user);
        return "redirect:/profile";
    }
}