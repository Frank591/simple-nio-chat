package ru.fsl.chat.client;

public interface Printer {

    void printFromUser(String userName, String text);

    void printSystem(String text);
}
