package org.example.cdsmarkaztelegrambot.enums;

import lombok.Getter;

@Getter
public enum Messages {

    MAIN_MENU("CDS Markaz botimizga xush kelibsiz \n"),
    START("/start"),
    NAME("Toliq ism familiyangizni kiriting"),
    PHONE_NUMBER("Raqamingizni yuboring"),
    RATE_SUCCESSFUL("✅ Bahoyingiz qabul qilindi: \" + \"⭐\""),;

    private final String label;

    Messages(String label) {
        this.label = label;
    }

}
