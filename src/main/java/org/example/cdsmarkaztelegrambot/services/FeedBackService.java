package org.example.cdsmarkaztelegrambot.services;

import lombok.RequiredArgsConstructor;
import org.example.cdsmarkaztelegrambot.models.FeedBackMessage;
import org.example.cdsmarkaztelegrambot.models.ScheduledFeedback;
import org.example.cdsmarkaztelegrambot.repositories.FeedBackMessageRepository;
import org.example.cdsmarkaztelegrambot.repositories.ScheduledFeedbackRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedBackService {

    private final ScheduledFeedbackRepository scheduledFeedbackRepository;
    private final FeedBackMessageRepository feedBackMessageRepository;

    public void rate(String uniqueName, String rate) {
        ScheduledFeedback feedback = scheduledFeedbackRepository.findByUniqueName(uniqueName);
        feedback.setRate(Integer.parseInt(rate));
        scheduledFeedbackRepository.save(feedback);
    }

    public void saveFeedbackMessage(String message) {
        FeedBackMessage feedbackMessage = new FeedBackMessage();
        feedbackMessage.setMessage(message);
        feedBackMessageRepository.save(feedbackMessage);
    }
}
