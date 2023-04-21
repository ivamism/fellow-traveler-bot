package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.handler.enums.Handlers;
import by.ivam.fellowtravelerbot.handler.enums.RegUserProcessStatus;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;





@Component
@Data
@Log4j
public class RegistrationHandler {

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
            message.setText("Поддвердите данные для регистрации: \n\n Ваше Имя - " + firstName);


        }

    void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }

//      userService.registerNewUser(regUser);



    private void registerUser(long chatId) {


//        {
//


//        message.setReplyMarkup(markupInLine);

//        executeMessage(message);
    }



}
