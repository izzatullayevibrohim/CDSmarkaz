package org.example.cdsmarkaztelegrambot.telegramBot.botService;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.List;

@Service
public class KeyboardService {

    public ReplyKeyboardMarkup phoneNumber(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardButton phoneNumberButton = new KeyboardButton("Telefon raqam yuborish 📲");
        phoneNumberButton.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();

        row.add(phoneNumberButton);
        replyKeyboardMarkup.setKeyboard(List.of(row));
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }
}
