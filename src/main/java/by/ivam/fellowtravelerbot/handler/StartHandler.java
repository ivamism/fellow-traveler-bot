package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Data
@Log4j
public class StartHandler {
    @Autowired
    RegistrationHandler registrationHandler;
    @Autowired
    Messages messages;

    @Autowired
    UserService userService;


    public String startMessaging(long chatId, Message incomeMessage) {
        String answer;

        if (userService.findById(chatId).isEmpty()) {
            log.info("User " + incomeMessage.getChat().getUserName()
                    + ". ChatId: " + chatId + "is new. Call registration process.");
            answer = messages.getSTART_REGISTRATION();
            registrationHandler.startRegistration(incomeMessage);
        } else {
            answer = messages.getCHOOSE_ACTION();
        }

        return answer;
    }


}
