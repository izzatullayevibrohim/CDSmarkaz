package org.example.cdsmarkaztelegrambot.repositories;

import org.example.cdsmarkaztelegrambot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByTelegramUsername(String username);
    Long countByIsActive(Boolean isActive);
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByRoleIdIsNull();
    @Query("select u.telegramUsername from User u where u.isActive = true")
    List<String> findTelegramUsernameByIsActiveTrue();
}
