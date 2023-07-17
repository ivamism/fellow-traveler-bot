package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@Data
@Log4j
public class StartHandler {
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
    SendMessage message = new SendMessage();


    public boolean checkRegistration(long chatId) {
        return userService.findById(chatId).isEmpty();
    }

    public SendMessage startMessaging(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();

        if (checkRegistration(chatId)) {

            message = userHandler.startRegistration(chatId);

            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + " is new User. Call registration process.");
        } else {
            message.setChatId(chatId);
            message.setText(messages.getFURTHER_ACTION_MESSAGE());
            message.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + " is registered User. Suggested to choose next step.");
        }
        return message;
    }

    public SendMessage noRegistrationMessage(long chatId) {
        message.setChatId(chatId);
        message.setText(messages.getNO_REGISTRATION_MESSAGE());
        return message;
    }
}
