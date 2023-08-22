package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@Data
@Log4j
public class StartHandler implements Handler {
    @Autowired
    Messages messages;
    @Autowired
    UserService userService;
    @Autowired
    Keyboards keyboards;
    @Autowired
    Buttons buttons;
    @Autowired
    UserHandler userHandler;
    @Autowired
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    ResponseMessageProcessor messageProcessor;


    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        log.debug("method handleReceivedMessage");
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug("method handleReceivedCallback");
        switch (callback) {
            case "CANCEL_CALLBACK" -> {

                editMessage = quitProcessMessage(incomeMessage);
            }
        }
        messageProcessor.sendEditedMessage(editMessage);
    }

    public boolean checkRegistration(long chatId) {
        return userService.findById(chatId).isEmpty();
    }

    public void startCommandReceived(Message incomeMessage) {
        log.info("Start command received");
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText("Привет, " + incomeMessage.getChat().getFirstName() + "!");
        sendMessage.setReplyMarkup(keyboards.mainMenu());
        messageProcessor.sendMessage(sendMessage);
//        sendMessage(message);
        startMessaging(incomeMessage);
    }


    public void startMessaging(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();

        if (checkRegistration(chatId)) {

            userHandler.startRegistration(chatId);

            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + " is new User. Call registration process.");
        } else {
            sendMessage.setChatId(chatId);
            sendMessage.setText(messages.getFURTHER_ACTION_MESSAGE());
            sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + " is registered User. Suggested to choose next step.");
//        sendMessage(message);
            messageProcessor.sendMessage(sendMessage);
        }
    }

    public void noRegistrationMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getNO_REGISTRATION_MESSAGE());
        messageProcessor.sendMessage(sendMessage);
    }

    public EditMessageText noRegistrationEditMessage(long chatId) {
        editMessage.setChatId(chatId);
        editMessage.setMessageId(editMessage.getMessageId());
        editMessage.setText(messages.getNO_REGISTRATION_MESSAGE());
        return editMessage;
    }

    private EditMessageText quitProcessMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setChatId(chatId);
        editMessage.setText(messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        log.info("method quitProcessMessage: Quit the process");
        chatStatusStorageAccess.deleteChatStatus(chatId);
        return editMessage;
    }
}
