package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public class Handler {
    @Autowired
    Messages messages;
    @Autowired
    Keyboards keyboards;
    @Autowired
    Buttons buttons;
    @Autowired
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    ResponseMessageProcessor messageProcessor;

    public String trimProcess(String s) {
        return s.split(":")[0];
    }

    public String trimSecondSubstring(String s) {
        return s.split(":")[1];
    }

    public int trimId(String s) {
        return Integer.parseInt(s.split(":")[1]);
    }

    public String firstLetterToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public void sendBotMessage(SendMessage message) {
        messageProcessor.sendMessage(message);
    }

    public void sendEditMessage(EditMessageText editMessage) {
        messageProcessor.sendEditedMessage(editMessage);
    }
}
