package org.example.cdsmarkaztelegrambot.services;

import lombok.RequiredArgsConstructor;
import org.example.cdsmarkaztelegrambot.models.FeedBackMessage;
import org.example.cdsmarkaztelegrambot.models.ScheduledFeedback;
import org.example.cdsmarkaztelegrambot.repositories.FeedBackMessageRepository;
import org.example.cdsmarkaztelegrambot.repositories.ScheduledFeedbackRepository;
import org.example.cdsmarkaztelegrambot.telegramBot.handler.MessageHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeedbackScheduler {

    private final ScheduledFeedbackRepository scheduledFeedbackRepository;
    private final MessageHandler messageHandler;
    private final FeedBackMessageRepository feedBackMessageRepository;

    @Scheduled(fixedRate = 60_000)
    public void sendDueFeedbacks() {
        List<ScheduledFeedback> due = scheduledFeedbackRepository
                .findAllBySentFalseAndScheduledAtBefore(LocalDateTime.now());

        for (ScheduledFeedback feedback : due) {
            try {

                Optional<FeedBackMessage> feedBack = feedBackMessageRepository.findTopByOrderByCreatedAtDesc();
                FeedBackMessage feedbackMessage = feedBack.orElseThrow(() -> new RuntimeException("Feedback message not found"));


                feedback.setSent(true);
                feedback.setSentAt(LocalDateTime.now());
                feedback.setUniqueName(UUID.randomUUID().toString().replace("-", ""));
                scheduledFeedbackRepository.save(feedback);
                SendMessage sm = new SendMessage();
                sm.setChatId(feedback.getUserChatId());
                sm.setText(feedbackMessage.getMessage());
                messageHandler.getTelegramBot().execute(sm);

            } catch (Exception e) {
                System.err.println("Feedback xatolik: " + e.getMessage());
            }
        }
    }
}