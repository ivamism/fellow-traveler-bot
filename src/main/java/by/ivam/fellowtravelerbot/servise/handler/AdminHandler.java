package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


/*
This class handle Admin functional
 */
@Service
@Data
@Log4j
public class AdminHandler {
    @Autowired
    UserService userService;
    @Autowired
    Messages messages;
    @Autowired
    Keyboards keyboards;

//    TODO Добавить логирование

    SendMessage sendMessage = new SendMessage();
    public boolean checkIsAdmin (long chatId){

        return userService.findUserById(chatId).isAdmin();
    }

    public SendMessage showAdminMenuMessage(long chatId){
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADMIN_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.mainAdminMenu());
        return sendMessage;
    }


}
