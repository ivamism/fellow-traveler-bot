package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface Handler {


    default void sendMessage (SendMessage message){
        ResponseMessageProcessor messageProcessor = new ResponseMessageProcessor();
        messageProcessor.sendMessage(message);
    }
    default void sendEditMessage (EditMessageText editMessage){
        ResponseMessageProcessor messageProcessor = new ResponseMessageProcessor();
        messageProcessor.sendEditedMessage(editMessage);
    }
}
