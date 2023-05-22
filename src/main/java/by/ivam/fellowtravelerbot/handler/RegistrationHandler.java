package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.handler.enums.BotStatus;
import by.ivam.fellowtravelerbot.handler.storage.StorageAccess;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;


@Component
@Data
@Log4j
public class RegistrationHandler {
    @Autowired
    Messages messages;
    @Autowired
    UserService userService;

    @Autowired
    Keyboards keyboards;

    @Autowired
    Buttons buttons;
    @Autowired
    StorageAccess storageAccess;


    RegUser regUser = new RegUser();


    public EditMessageText checkRegData(int messageId, long chatId, String userName){

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(messages.getCONFIRM_REG_DATA_MESSAGE() + userName + "?");
        message.setMessageId(messageId);
        message.setReplyMarkup(keyboards.threeButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getCONFIRM_REG_DATA_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getDENY_REG_CALLBACK()));

        log.debug("Send request for confirmation of registration data");

//        storageAccess.addChatStatus(messageId, String.valueOf(BotStatus.WAIT_CONFIRMATION));
        return message;
    }

    public void userRegistration(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String firstName = incomeMessage.getChat().getFirstName();
        String userName = incomeMessage.getChat().getUserName();
        regUser.setChatId(chatId)
                .setFirstName(firstName)
                .setTelegramUserName(userName);

        userService.registerNewUser(regUser);
//        storageAccess.deleteChatStatus(incomeMessage.getMessageId());
        log.debug("Start registration process with user: " + regUser);
    }





}
