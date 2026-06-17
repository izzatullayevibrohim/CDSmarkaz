package org.example.cdsmarkaztelegrambot.security;

import lombok.RequiredArgsConstructor;
import org.example.cdsmarkaztelegrambot.models.Role;
import org.example.cdsmarkaztelegrambot.models.User;
import org.example.cdsmarkaztelegrambot.repositories.RoleRepository;
import org.example.cdsmarkaztelegrambot.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByTelegramUsername(username).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User topilmadi: " + username);
        }

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new UsernameNotFoundException("Rol topilmadi"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getTelegramUsername())
                .password(user.getPassword())
                .authorities(role.getName())
                .build();
    }
}