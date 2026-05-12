package org.example.cdsmarkaztelegrambot.telegramBot.handler;

import lombok.RequiredArgsConstructor;
import org.example.cdsmarkaztelegrambot.enums.Messages;
import org.example.cdsmarkaztelegrambot.services.FeedBackService;
import org.example.cdsmarkaztelegrambot.telegramBot.TelegramBot;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CallBackHandler {

    private final ApplicationContext applicationContext;

    private final FeedBackService feedBackService;

    public TelegramBot getTelegramBot() {
        return (TelegramBot) applicationContext.getBean("telegramBot");
    }

    public void handler(CallbackQuery callbackQuery) {
        SendMessage sendMessage = new SendMessage();
        String data = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        sendMessage.setChatId(chatId);

        DeleteMessage deleteMessage = new DeleteMessage();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        if (data.startsWith("fb_")){
            System.out.println("1.Data { "+data+" }");
            data = data.replace("fb_", "");
            System.out.println("2.Data { "+data+" }");
            String rate = data.substring(data.length()-1);
            System.out.println("Rate { "+rate+" }");
            String uniqueName = data.substring(0,data.lastIndexOf("_"));
            System.out.println("Unique name { "+uniqueName+" }");

            feedBackService.rate(uniqueName, rate);

            EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
            editMarkup.setChatId(chatId);
            editMarkup.setMessageId(messageId);
            editMarkup.setReplyMarkup(new InlineKeyboardMarkup(List.of()));

            EditMessageText editText = new EditMessageText();
            editText.setChatId(chatId);
            editText.setMessageId(messageId);
            editText.setText(Messages.RATE_SUCCESSFUL.getLabel());

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());

            try {
                getTelegramBot().execute(editText);
                getTelegramBot().execute(answer);
            } catch (Exception e) {
                System.err.println("Xatolik: " + e.getMessage());
            }
        }
    }
}
