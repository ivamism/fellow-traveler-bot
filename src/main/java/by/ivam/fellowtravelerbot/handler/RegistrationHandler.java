package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.DTO.RegUser;
import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.handler.enums.ChatStatus;
import by.ivam.fellowtravelerbot.storages.StorageAccess;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    // Ask user to confirm telegram user first name as UserName or edit it
    public EditMessageText confirmUserFirstName(int messageId, long chatId, String userName) {

        message.setChatId(chatId);
        message.setText(messages.getCONFIRM_USER_FIRST_MESSAGE() + userName + "?");
        message.setMessageId(messageId);
        message.setReplyMarkup(keyboards.threeButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getCONFIRM_REG_DATA_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getDENY_REG_CALLBACK()));

        log.debug("method confirmUserFirstName. Send request for confirmation of registration data");

        return message;
    }

    //    Request to send users choice of userFirstName
    public EditMessageText editUserFirstName(Message incomeMessage) {

        long chatId = incomeMessage.getChatId();
        message.setMessageId(incomeMessage.getMessageId());
        message.setText(messages.getEDIT_USER_FIRSTNAME_MESSAGE());
        message.setReplyMarkup(null); //need to use null to remove no longer necessary inline keyboard

        storageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.REGISTRATION_EDIT_NAME));
        log.debug("method editUserFirstName. Send request to enter the first or nick name of User");
        return message;
    }

//    request user to confirm that edited userFirstName is correct
    public SendMessage confirmEditedUserFirstName(Message incomeMessage) {
        SendMessage sendMessage = new SendMessage();
        int messageId = incomeMessage.getMessageId();
        long chatId = incomeMessage.getChatId();
        String incomeMessageText = incomeMessage.getText();

        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCONFIRM_FIRSTNAME_MESSAGE() + incomeMessageText);
        sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getNAME_TO_CONFIRM_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK()));
        storageAccess.addUserFirstName(chatId, incomeMessageText);
        log.info("method confirmEditedUserFirstName. Send request to confirm edited name");
        return sendMessage;
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
        message.setReplyMarkup(null); //need to use null to remove no longer necessary inline keyboard

        storageAccess.deleteChatStatus(chatId);
        log.debug("method userRegistration. Call saving to DB user: " + regUser);
        return message;
    }

    public EditMessageText userRegistration(Message incomeMessage, String firstName) {
        Long chatId = incomeMessage.getChatId();
        String userName = incomeMessage.getChat().getUserName();
        regUser.setChatId(chatId)
                .setFirstName(firstName)
                .setTelegramUserName(userName);

        userService.registerNewUser(regUser);

        message.setMessageId(incomeMessage.getMessageId());
        message.setText(messages.getSUCCESS_REGISTRATION_MESSAGE());
        message.setReplyMarkup(null); //need to use null to remove no longer necessary inline keyboard

        storageAccess.deleteChatStatus(chatId);
        storageAccess.deleteUserFirstName(chatId);
        log.debug("method userRegistration. Call saving to DB with with edited firstname: " + regUser);
        return message;
    }

    public EditMessageText denyRegistration(Message incomeMessage) {
        message.setMessageId(incomeMessage.getMessageId());
        message.setText(messages.getDENY_REGISTRATION_MESSAGE());
        message.setChatId(incomeMessage.getChatId());
        log.info("User deny registration");
        message.setReplyMarkup(null); //need to use null to remove no longer necessary inline keyboard
        return message;
//  TODO Добавить сообщение о невозможности пользоваться ботом без регистрации и предложить вернутся к регистрации
//  TODO Реализовать процесс возврата к регистрации
    }
}
