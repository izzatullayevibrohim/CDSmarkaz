package org.example.cdsmarkaztelegrambot.services;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.example.cdsmarkaztelegrambot.models.User;
import org.example.cdsmarkaztelegrambot.repositories.UserRepository;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // UserService
    public Optional<User> findByUsername(String username) {
        return userRepository.findByTelegramUsername(username);
    }
    public void updateUserStatusWithStatus(String newStatus, String chatId) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            if (newStatus.equals("kicked") || newStatus.equals("left")) {
                user.setIsActive(false);
            } else if (newStatus.equals("member")) {
                user.setIsActive(true);
            }
            userRepository.save(user);
        });
    }

    public void setStatus(String chatId, boolean isActive) {
        userRepository.findByChatId(chatId).ifPresentOrElse(
                user -> {
                    user.setIsActive(isActive);
                    userRepository.save(user);
                },
                () -> log.warn("User not found by chatId: {}", chatId)
        );
    }

    public void save(User user) {
        userRepository.save(user);
    }
}
