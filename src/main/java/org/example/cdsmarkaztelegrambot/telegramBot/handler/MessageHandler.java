package org.example.cdsmarkaztelegrambot.telegramBot.handler;

import org.example.cdsmarkaztelegrambot.enums.Messages;
import org.example.cdsmarkaztelegrambot.enums.UserState;
import org.example.cdsmarkaztelegrambot.models.*;
import org.example.cdsmarkaztelegrambot.repositories.CheckMessageRepository;
import org.example.cdsmarkaztelegrambot.repositories.ScheduledFeedbackRepository;
import org.example.cdsmarkaztelegrambot.repositories.UserRepository;
import org.example.cdsmarkaztelegrambot.repositories.WelcomeMessageRepository;
import org.example.cdsmarkaztelegrambot.services.MediaFileService;
import org.example.cdsmarkaztelegrambot.services.UserService;
import org.example.cdsmarkaztelegrambot.telegramBot.TelegramBot;
import org.example.cdsmarkaztelegrambot.telegramBot.botService.KeyboardService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageHandler {

    private final ApplicationContext applicationContext;
    private final UserService userService;
    KeyboardService keyboardService;
    UserRepository userRepository;
    WelcomeMessageRepository welcomeMessageRepository;
    MediaFileService mediaFileService;
    CheckMessageRepository checkMessageRepository;
    ScheduledFeedbackRepository scheduledFeedbackRepository;

    public MessageHandler(UserRepository userRepository,
                          KeyboardService keyboardService,
                          WelcomeMessageRepository welcomeMessageRepository,
                          MediaFileService mediaFileService,
                          CheckMessageRepository checkMessageRepository,
                          ScheduledFeedbackRepository scheduledFeedbackRepository,
                          ApplicationContext applicationContext, UserService userService) {
        this.userRepository = userRepository;
        this.keyboardService = keyboardService;
        this.welcomeMessageRepository = welcomeMessageRepository;
        this.mediaFileService = mediaFileService;
        this.checkMessageRepository = checkMessageRepository;
        this.scheduledFeedbackRepository = scheduledFeedbackRepository;
        this.applicationContext = applicationContext;
        this.userService = userService;
    }

    Map<String, UserState> userStates = new HashMap<>();
    Map<String, User> tempData = new HashMap<>();

    public TelegramBot getTelegramBot() {
        return applicationContext.getBean(TelegramBot.class);
    }

    public void handle(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        String chatId = message.getChatId().toString();
        sendMessage.setChatId(chatId);

        if (message.hasText() && message.getText().equals(Messages.START.getLabel())) {
            String username = message.getFrom().getUserName();
            List<String> allUsernames = userService.getAllUsernames();
            if (allUsernames.contains(username)) {
                sendMessage.setText(Messages.MAIN_MENU.getLabel() + username);
                getTelegramBot().sendMessage(sendMessage);
                return;
            }

            userStates.put(chatId, UserState.REGISTRATION_NAME);
            sendMessage.setText(Messages.MAIN_MENU.getLabel() + Messages.NAME.getLabel());
            getTelegramBot().sendMessage(sendMessage);

        } else if (userStates.get(chatId) == UserState.REGISTRATION_NAME) {
            User user = new User(
                    message.getText(),
                    message.getFrom().getUserName(),
                    chatId
            );
            tempData.put(chatId, user);
            userStates.put(chatId, UserState.REGISTRATION_NUMBER);
            sendMessage.setText(Messages.PHONE_NUMBER.getLabel());
            sendMessage.setReplyMarkup(keyboardService.phoneNumber());
            getTelegramBot().sendMessage(sendMessage);

        } else if (message.hasContact() && userStates.get(chatId) == UserState.REGISTRATION_NUMBER) {
            User user = tempData.get(chatId);
            user.setPhoneNumber(message.getContact().getPhoneNumber());
            userStates.put(chatId, UserState.NONE);

            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
            replyKeyboardRemove.setRemoveKeyboard(true);
            sendMessage.setReplyMarkup(replyKeyboardRemove);

            WelcomeMessage welcomeMessage = welcomeMessageRepository.findTopByOrderByCreatedAtDesc();
            if (welcomeMessage.getMediaFileId() != null) {
                MediaFile mediaFile = mediaFileService.getMediaFileById(welcomeMessage.getMediaFileId());
                if (mediaFile != null) {
                    switch (mediaFile.getFileType()) {
                        case "image":
                            SendPhoto sendPhoto = new SendPhoto();
                            sendPhoto.setChatId(chatId);
                            sendPhoto.setPhoto(new InputFile(new File(mediaFile.getFilePath())));
                            getTelegramBot().execute(sendPhoto);
                            break;
                        case "video":
                            SendVideo sendVideo = new SendVideo();
                            sendVideo.setChatId(chatId);
                            sendVideo.setVideo(new InputFile(new File(mediaFile.getFilePath())));
                            getTelegramBot().execute(sendVideo);
                            break;
                        case "audio":
                            SendAudio sendAudio = new SendAudio();
                            sendAudio.setChatId(chatId);
                            sendAudio.setAudio(new InputFile(new File(mediaFile.getFilePath())));
                            getTelegramBot().execute(sendAudio);
                            break;
                    }
                }
                sendMessage.setText(welcomeMessage.getMessage());
            }
            getTelegramBot().sendMessage(sendMessage);

        } else {
            System.out.println("System error!!!!");
        }
    }

    public void broadcastAndSchedule(CheckMessage checkMessage) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getChatId() == null) continue;
            sendToUser(user.getChatId(), checkMessage);
        }

        checkMessage.setSentAt(LocalDateTime.now());
        CheckMessage saved = checkMessageRepository.save(checkMessage);

        LocalDateTime feedbackTime = LocalDateTime.now()
                .plusMinutes(checkMessage.getDelayMinutes());

        for (User user : users) {
            if (user.getChatId() == null) continue;

            ScheduledFeedback feedback = ScheduledFeedback.builder()
                    .checkMessageId(saved.getId())
                    .userChatId(user.getChatId())
                    .scheduledAt(feedbackTime)
                    .sent(false)
                    .build();

            scheduledFeedbackRepository.save(feedback);
        }
    }

    private void sendToUser(String chatId, CheckMessage msg) {
        try {
            if (msg.getMediaFileId() != null) {
                MediaFile mediaFile = mediaFileService.getMediaFileById(msg.getMediaFileId());
                if (mediaFile != null) {

                    byte[] fileBytes = mediaFileService.downloadFile(mediaFile.getFilePath());
                    InputStream inputStream = new ByteArrayInputStream(fileBytes);
                    InputFile inputFile = new InputFile(inputStream, mediaFile.getOriginalName());

                    switch (mediaFile.getFileType()) {
                        case "image" -> {
                            SendPhoto sp = new SendPhoto();
                            sp.setChatId(chatId);
                            sp.setPhoto(inputFile);
                            if (msg.getCaption() != null) sp.setCaption(msg.getCaption());
                            getTelegramBot().execute(sp);
                        }
                        case "video" -> {
                            SendVideo sv = new SendVideo();
                            sv.setChatId(chatId);
                            sv.setVideo(inputFile);
                            if (msg.getCaption() != null) sv.setCaption(msg.getCaption());
                            getTelegramBot().execute(sv);
                        }
                        case "audio" -> {
                            SendAudio sa = new SendAudio();
                            sa.setChatId(chatId);
                            sa.setAudio(inputFile);
                            getTelegramBot().execute(sa);
                        }
                    }
                }
            }

            if (msg.getMessage() != null && !msg.getMessage().isBlank()) {
                SendMessage sm = new SendMessage();
                sm.setChatId(chatId);
                sm.setText(msg.getMessage());
                getTelegramBot().execute(sm);
            }

        } catch (Exception e) {
            System.err.println("Yuborishda xatolik [" + chatId + "]: " + e.getMessage());
        }
    }
}