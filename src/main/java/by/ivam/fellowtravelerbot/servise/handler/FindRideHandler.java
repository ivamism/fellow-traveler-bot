package by.ivam.fellowtravelerbot.servise.handler;


import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;


// This class handle operations with search of hitchhiker's rides
@Service
@Data
@Log4j
public class FindRideHandler extends Hndlr implements HandlerInterface{
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();
    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        log.debug("method handleReceivedMessage");
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug("method handleReceivedCallback. get callback: " + callback);
    }


    public void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }
}
