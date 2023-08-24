package by.ivam.fellowtravelerbot.bot.dispatcher;

import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

public class Dispatcher {
    @Autowired
    ResponseMessageProcessor messageProcessor;

    public void sendMessage (SendMessage responseMessage){
        messageProcessor.sendMessage(responseMessage);
    }
    public void sendEditedMessage (EditMessageText responseMessage){
        messageProcessor.sendEditedMessage(responseMessage);
    }


    public String getHandler(String s) {
        return s.split("-")[0];
    }

    public String getProcess(String s) {
        return s.split("-")[1];
    }

}
