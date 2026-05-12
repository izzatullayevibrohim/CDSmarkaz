package org.example.cdsmarkaztelegrambot.repositories;

import org.example.cdsmarkaztelegrambot.models.WelcomeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WelcomeMessageRepository extends JpaRepository<WelcomeMessage, Long> {

    WelcomeMessage findTopByOrderByCreatedAtDesc();
}
