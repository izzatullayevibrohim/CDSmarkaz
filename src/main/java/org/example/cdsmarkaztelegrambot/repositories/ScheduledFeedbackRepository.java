package org.example.cdsmarkaztelegrambot.repositories;

import org.example.cdsmarkaztelegrambot.models.ScheduledFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledFeedbackRepository extends JpaRepository<ScheduledFeedback, Long> {

    List<ScheduledFeedback> findAllBySentFalseAndScheduledAtBefore(LocalDateTime now);

    ScheduledFeedback findByUniqueName(String uniqueName);
}