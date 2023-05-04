package by.ivam.fellowtravelerbot.handler;

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
public class RegistrationHandler {
    @Autowired
    Messages messages;
    @Autowired
    UserService userService;


    RegUser regUser = new RegUser();

    public void startRegistration(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String firstName = incomeMessage.getChat().getFirstName();
        String userName = incomeMessage.getChat().getUserName();
        regUser.setChatId(chatId)
                .setFirstName(firstName)
                .setTelegramUserName(userName);

        log.debug("Start registration process with user: " + regUser);
        SendMessage message = new SendMessage();
        message.setChatId((chatId));
        message.setText(messages.getCONFIRM_REG_DATA() + firstName);
        registerUser(chatId);

    }


    private void registerUser(long chatId) {
        userService.registerNewUser(regUser);
    }

//        {
////
//
//
////        message.setReplyMarkup(markupInLine);
//
////        executeMessage(message);
//    }


}
