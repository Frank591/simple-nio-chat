package ru.fsl.chat.client;

import org.jetbrains.annotations.NotNull;

public class ConsolePrinter implements Printer {

    @Override
    public void printFromUser(@NotNull String userName, @NotNull String text) {
        System.out.println(String.format("%s: %s", userName, text));
    }

    @Override
    public void printSystem(String text) {
        System.out.println(text);
    }
}
