package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
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


    public SendMessage startMessaging(long chatId, Message incomeMessage) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (userService.findById(chatId).isEmpty()) {

            message.setText(messages.getSTART_REGISTRATION());
            message.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getCONFIRM_START_REG_CALLBACK(), buttons.getNO_BUTTON_TEXT(), buttons.getDENY_REG_CALLBACK()));

            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + " is new User. Call registration process.");

        } else {

            message.setText(messages.getCHOOSE_ACTION());
        }

        return message;
    }
}
