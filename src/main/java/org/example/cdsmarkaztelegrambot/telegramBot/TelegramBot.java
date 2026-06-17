package org.example.cdsmarkaztelegrambot.telegramBot;

import org.example.cdsmarkaztelegrambot.services.UserService;
import org.example.cdsmarkaztelegrambot.telegramBot.handler.CallBackHandler;
import org.example.cdsmarkaztelegrambot.telegramBot.handler.MessageHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Lazy
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final MessageHandler messageHandler;
    private final CallBackHandler callBackHandler;
    private final UserService userService;

    public TelegramBot(MessageHandler messageHandler, CallBackHandler callBackHandler, UserService userService) {
        super("8406880553:AAF9RQU0ciik-izrj_59osccVHKgmiQQK7o");
        this.messageHandler = messageHandler;
        this.callBackHandler = callBackHandler;
        this.userService = userService;
    }


    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            try {
                this.messageHandler.handle(update.getMessage());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (update.hasMyChatMember()) {
            ChatMemberUpdated cmu = update.getMyChatMember();
            String chatId = String.valueOf(cmu.getChat().getId());
            String newStatus = cmu.getNewChatMember().getStatus();
            userService.updateUserStatusWithStatus(newStatus, chatId);
        } else if (update.hasCallbackQuery()) {
                callBackHandler.handler(update.getCallbackQuery());
            }
        }

    @Override
    public String getBotUsername() {
        return "cdsmarkazbot";
    }

    public void sendMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            if (e.getMessage() != null && e.getMessage().contains("bot was blocked")) {
                userService.setStatus(message.getChatId(), false);
            }
        }
    }
}
