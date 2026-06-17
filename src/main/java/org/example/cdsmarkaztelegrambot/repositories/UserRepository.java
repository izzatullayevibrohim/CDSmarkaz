package org.example.cdsmarkaztelegrambot.repositories;

import org.example.cdsmarkaztelegrambot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramUsername(String telegramUsername);

    Long countByIsActive(Boolean isActive);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<User> findByRoleIdIsNull();

    Optional<User> findByChatId(String chatId);

    long countByRoleIdIsNull();
}