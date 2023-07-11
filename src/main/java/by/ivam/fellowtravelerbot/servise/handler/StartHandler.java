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

    public boolean checkRegistration (long chatId){
        return userService.findById(chatId).isEmpty();
    }
    SendMessage message = new SendMessage();

    public SendMessage startMessaging(long chatId, Message incomeMessage) {
        message.setChatId(chatId);

        if (checkRegistration(chatId)) {

// TODO Стоит ли вынести в метод в registrationHandler

            message.setText(messages.getSTART_REGISTRATION());
            message.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(),
                    buttons.getCONFIRM_START_REG_CALLBACK(),
                    buttons.getNO_BUTTON_TEXT(),
                    buttons.getDENY_REG_CALLBACK()));

            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + " is new User. Call registration process.");
        } else {
            message.setText(messages.getFURTHER_ACTION_MESSAGE());
            message.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + " is registered User. Suggested to choose next step.");
        }
        return message;
    }

    public SendMessage noRegistrationMessage (long chatId){
        message.setChatId(chatId);
        message.setText(messages.getNO_REGISTRATION_MESSAGE());
        return message;
    }
}
