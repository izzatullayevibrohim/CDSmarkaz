package org.example.cdsmarkaztelegrambot.enums;

import lombok.Getter;

@Getter
public enum MediaFileType {

    PHOTO("image"),
    VIDEO("video"),
    AUDIO("audio"),
    TEXT("text");

    private final String label;

    MediaFileType(String label) {
        this.label = label;
    }
}
