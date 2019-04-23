package ru.fsl.chat.contracts.dto;

import org.jetbrains.annotations.NotNull;

public class PrintMessageRequest {

    public static final int MAX_MESSAGE_TEXT_LENGTH = 140;

    private final String text;

    /**
     * Only for manual use. It doesn't use inside gson deserizliation.
     */
    public PrintMessageRequest(@NotNull String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
