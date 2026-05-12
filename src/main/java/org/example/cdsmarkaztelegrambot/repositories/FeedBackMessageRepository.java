package org.example.cdsmarkaztelegrambot.repositories;

import org.example.cdsmarkaztelegrambot.models.FeedBackMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FeedBackMessageRepository extends JpaRepository<FeedBackMessage, Long> {

    Optional<FeedBackMessage> findTopByOrderByCreatedAtDesc();
}
