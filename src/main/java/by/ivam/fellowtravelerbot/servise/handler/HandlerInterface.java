package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface HandlerInterface {

    void handleReceivedMessage(String chatStatus, Message incomeMessage);
    void handleReceivedCallback(String callback, Message incomeMessage);


}
