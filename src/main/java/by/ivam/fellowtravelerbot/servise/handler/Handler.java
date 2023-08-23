package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface Handler {

    void handleReceivedMessage(String chatStatus, Message incomeMessage);
    void handleReceivedCallback(String callback, Message incomeMessage);

    default String trimProcess(String s) {
        return s.split(":")[0];
    }
    default String trimSecondSubstring(String s) {
        return s.split(":")[1];
    }
    default int trimId(String s) {
        return Integer.parseInt(s.split(":")[1]);
    }
    default String firstLetterToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    default void sendMessage (SendMessage message){
        ResponseMessageProcessor messageProcessor = new ResponseMessageProcessor();
        messageProcessor.sendMessage(message);
    }
    default void sendEditMessage (EditMessageText editMessage){
        ResponseMessageProcessor messageProcessor = new ResponseMessageProcessor();
        messageProcessor.sendEditedMessage(editMessage);
    }

}
