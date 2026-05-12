package org.example.cdsmarkaztelegrambot.services;

import org.example.cdsmarkaztelegrambot.models.WelcomeMessage;
import org.example.cdsmarkaztelegrambot.repositories.WelcomeMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class WelcomeMessageService {

    private final WelcomeMessageRepository welcomeMessageRepository;

    public WelcomeMessageService(WelcomeMessageRepository welcomeMessageRepository) {
        this.welcomeMessageRepository = welcomeMessageRepository;
    }

    public void saveWelcomeMessage(String type, String message, String caption, Long MediaFileId) {

        WelcomeMessage welcomeMessage = new WelcomeMessage();
        welcomeMessage.setType(type);
        welcomeMessage.setMessage(message);
        welcomeMessage.setCaption(caption);
        welcomeMessage.setMediaFileId(MediaFileId);
        welcomeMessageRepository.save(welcomeMessage);
    }

    public void saveOnlyMessage(String message){
        WelcomeMessage welcomeMessage = new WelcomeMessage();
        welcomeMessage.setMessage(message);
        welcomeMessageRepository.save(welcomeMessage);
    }
}
