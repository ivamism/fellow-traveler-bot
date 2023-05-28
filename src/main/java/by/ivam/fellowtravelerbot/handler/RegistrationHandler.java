package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.DTO.RegUser;
import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.handler.enums.BotStatus;
import by.ivam.fellowtravelerbot.handler.storages.StorageAccess;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;


/*
This class handle User registration process and saving User to DB
 */


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

    @Autowired
    RegUser regUser;
    EditMessageText message = new EditMessageText();

//    TODO Реализовать сохранение имени юзера после ввода до момента подтверждения и сохранения в БД
//    создать хранилище для имен (hash map <long chatId, String userName>
//    создать перегруженный метод регистрации юзера, принимающий параметром имя из хранилища
//    или изменить тип ваалью мапы на ДТО хранящий и статус и имя


    // Ask user to confirm telegram user first name as UserName or edit it
    public EditMessageText checkRegData(int messageId, long chatId, String userName) {

        message.setChatId(chatId);
        message.setText(messages.getCONFIRM_REG_DATA_MESSAGE() + userName + "?");
        message.setMessageId(messageId);
        message.setReplyMarkup(keyboards.threeButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getCONFIRM_REG_DATA_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getDENY_REG_CALLBACK()));

        log.debug("Send request for confirmation of registration data");

        return message;
    }

    //    Request to send users choice of userName
    public EditMessageText editUserName(Message incomeMessage) {
//        int messageId = incomeMessage.getMessageId();
        long chatId = incomeMessage.getChatId();
        message.setMessageId(incomeMessage.getMessageId());
        message.setText(messages.getEDIT_NAME());
//        message.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getNAME_CONFIRMED_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK()));
        message.setReplyMarkup(null); //need to use null to remove not necessary inline keyboard


        log.debug("Send request to enter the first or nick name of User");

        regUser.setChatId(chatId)
                .setBotStatus(BotStatus.REGISTRATION_EDIT_NAME);
        storageAccess.addRegUser(regUser);
//        storageAccess.addChatStatus(chatId, String.valueOf(BotStatus.REGISTRATION_EDIT_NAME));
        return message;
    }

//    request user to confirm that edited userName is correct
    public EditMessageText confirmEditRegData(Message incomeMessage) {
//        int messageId = incomeMessage.getMessageId();
        long chatId = incomeMessage.getChatId();
        message.setMessageId(incomeMessage.getMessageId());

        message.setText(messages.getCONFIRM_NAME() + incomeMessage.getText());
        message.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getNAME_TO_CONFIRM_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK()));
//        storageAccess.addChatStatus(chatId, String.valueOf(BotStatus.REGISTRATION_EDIT_NAME));
//        storageAccess.addChatStatus(chatId, String.valueOf(BotStatus.REGISTRATION_EDIT_NAME));
        log.info("Send request to confirm edited name");
        return message;
    }



    // Save User to DB
    public EditMessageText userRegistration(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String firstName = incomeMessage.getChat().getFirstName();
        String userName = incomeMessage.getChat().getUserName();
        regUser.setChatId(chatId)
                .setFirstName(firstName)
                .setTelegramUserName(userName);

        userService.registerNewUser(regUser);

        message.setMessageId(incomeMessage.getMessageId());
        message.setText(messages.getSUCCESS_REGISTRATION_MESSAGE());
        message.setReplyMarkup(null); //need to use null to remove not necessary inline keyboard

        storageAccess.deleteChatStatus(incomeMessage.getChatId());
        log.debug("Start registration process with user: " + regUser);
        return message;
    }


}
