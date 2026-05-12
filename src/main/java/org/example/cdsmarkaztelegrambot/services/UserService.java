package org.example.cdsmarkaztelegrambot.services;

import org.example.cdsmarkaztelegrambot.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<String> getAllUsernames() {
        return userRepository.findTelegramUsernameByIsActiveTrue();
    }
}
