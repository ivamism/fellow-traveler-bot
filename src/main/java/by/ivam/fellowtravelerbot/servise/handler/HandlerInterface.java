package by.ivam.fellowtravelerbot.servise.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface HandlerInterface {

    void handleReceivedMessage(String chatStatus, Message incomeMessage);
    void handleReceivedCallback(String callback, Message incomeMessage);


}
