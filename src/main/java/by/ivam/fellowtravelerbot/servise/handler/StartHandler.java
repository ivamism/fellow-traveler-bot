package by.ivam.fellowtravelerbot.servise.handler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

@EqualsAndHashCode(callSuper = true)
@Service
@Data
@Log4j
public class StartHandler extends BaseHandler implements HandlerInterface {
    @Autowired
    UserHandler userHandler;
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();


    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        log.debug("method handleReceivedMessage");
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug("method handleReceivedCallback");
        if (callback.equals("CANCEL_CALLBACK")) {
            editMessage = quitProcessMessage(incomeMessage);
        }
        sendEditMessage(editMessage);
    }

    public boolean checkRegistration(long chatId) {
        return userService.findById(chatId).isEmpty();
    }

    public void startCommandReceived(Message incomeMessage) {
        log.info("Start command received");
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText("Привет, " + incomeMessage.getChat().getFirstName() + "!");
        sendMessage.setReplyMarkup(keyboards.mainMenu());
        sendBotMessage(sendMessage);
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
            sendBotMessage(sendMessage);
        }
    }

    public void noRegistrationMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getNO_REGISTRATION_MESSAGE());
        sendBotMessage(sendMessage);
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
