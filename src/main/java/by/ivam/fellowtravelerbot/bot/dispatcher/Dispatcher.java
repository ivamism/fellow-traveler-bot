package by.ivam.fellowtravelerbot.bot.dispatcher;

import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.servise.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public class Dispatcher {
    @Autowired
    StartHandler startHandler;
    @Autowired
    AdminHandler adminHandler;
    @Autowired
    UserHandler userHandler;
    @Autowired
    CarHandler carHandler;
    @Autowired
    FindPassengerHandler findPassengerHandler;
    @Autowired
    FindRideHandler findRideHandler;
    @Autowired
    MatchingHandler matchingHandler;
    @Autowired
    ResponseMessageProcessor messageProcessor;

    public void sendBotMessage(SendMessage responseMessage){
        messageProcessor.sendMessage(responseMessage);
    }
    public void sendEditedMessage (EditMessageText responseMessage){
        messageProcessor.sendEditedMessage(responseMessage);
    }

    public String getHandler(String s) {
        return s.split("--")[0];
    }

    public String getProcess(String s) {
        return s.split("--")[1];
    }

}
