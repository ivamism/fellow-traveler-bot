package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.UserDTO;
import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.servise.handler.enums.ChatStatus;
import by.ivam.fellowtravelerbot.storages.StorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;


/*
This class handle User registration process and saving User to DB
 */


@Service
@Data
@Log4j
public class UserHandler {
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
    UserDTO userDTO;

    @Autowired
    CarHandler carHandler;
    EditMessageText editMessage = new EditMessageText();
    SendMessage sendMessage = new SendMessage();

    //  TODO Добавить сообщение о невозможности пользоваться ботом без регистрации и предложить вернутся к регистрации
//  TODO Реализовать процесс возврата к регистрации
//    TODO разделить функциональные действия и отправку сообщений

    // Ask user to confirm telegram User's first name as UserName or edit it
    public EditMessageText confirmUserFirstName(int messageId, long chatId, String userName) {

        editMessage.setChatId(chatId);
        editMessage.setText(messages.getCONFIRM_USER_FIRST_MESSAGE() + userName + "?");
        editMessage.setMessageId(messageId);
        editMessage.setReplyMarkup(keyboards.threeButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getCONFIRM_REG_DATA_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getDENY_REG_CALLBACK()));

        log.debug("method confirmUserFirstName. Send request for confirmation of registration data");

        return editMessage;
    }

    //    Request to send users choice of userFirstName
    public EditMessageText editUserFirstNameBeforeSaving(Message incomeMessage) {

        long chatId = incomeMessage.getChatId();
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getEDIT_USER_FIRSTNAME_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        storageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.REGISTRATION_USER_EDIT_NAME));
        log.debug("method editUserFirstNameBeforeSaving. Send request to enter User's firstname or nick");
        return editMessage;
    }

    //    request user to confirm that edited userFirstName is correct
    public SendMessage confirmEditedUserFirstName(Message incomeMessage) {

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
        userDTO.setChatId(chatId)
                .setFirstName(firstName)
                .setTelegramUserName(userName);

        userService.registerNewUser(userDTO);

        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getSUCCESS_REGISTRATION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        storageAccess.deleteChatStatus(chatId);
        log.debug("method userRegistration. Call saving to DB user: " + userDTO);
        return editMessage;
    }

    public EditMessageText userRegistration(Message incomeMessage, String firstName) {
        Long chatId = incomeMessage.getChatId();
        String userName = incomeMessage.getChat().getUserName();
        userDTO.setChatId(chatId)
                .setFirstName(firstName)
                .setTelegramUserName(userName);

        userService.registerNewUser(userDTO);

        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getSUCCESS_REGISTRATION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        storageAccess.deleteChatStatus(chatId);
        storageAccess.deleteUserFirstName(chatId);
        log.debug("method userRegistration. Call saving to DB with with edited firstname: " + userDTO);
        return editMessage;
    }

    public EditMessageText denyRegistration(Message incomeMessage) {
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getDENY_REGISTRATION_MESSAGE());
        editMessage.setChatId(incomeMessage.getChatId());
        log.info("User deny registration");
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }

//    Edit User's data

    private String getUserData(long chatId) {
        User user = userService.findUserById(chatId);
        return String.format(messages.getUSER_DATA(), user.getChatId(), user.getFirstName(), user.getUserName()) + carHandler.prepareCarListToSend(chatId);
    }

    public SendMessage sendUserData(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(getUserData(chatId));
        sendMessage.setReplyMarkup(keyboards.fiveButtonsColumnInlineKeyboard(buttons.getCHANGE_NAME_TEXT(), buttons.getEDIT_USER_NAME_CALLBACK(), buttons.getCHANGE_CAR_TEXT(), buttons.getEDIT_CAR_START_PROCESS_CALLBACK(), buttons.getDELETE_CAR_TEXT(), buttons.getREQUEST_DELETE_CAR_CALLBACK(), buttons.getDELETE_ALL_TEXT(), buttons.getDELETE_USER_START_PROCESS_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        log.info("send message with stored User's data and keyboard with further action menu");
        return sendMessage;
    }

    public EditMessageText editUserFirstNameMessage(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        String firstName = userService.findUserById(chatId).getFirstName();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getEDIT_USER_FIRSTNAME_MESSAGE() + String.format(messages.getEDIT_USER_FIRSTNAME_MESSAGE_POSTFIX(), firstName));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.USER_EDIT_NAME));
        log.debug("method editUserFirstNameBeforeSaving. Send request to enter User's firstname or nick");
        return editMessage;
    }

    public void saveEditedUserFirstName(long chatId, String firstName) {
        String oldFirstName = userService.findUserById(chatId).getFirstName();
        log.debug("User handler. Method saveEditedUserFirstName - call update User's first name from " + oldFirstName + " to " + firstName);
        userService.updateUserFirstName(chatId, firstName);
    }

    public SendMessage editUserFirstNameSuccessMessage(long chatId) {
        String firstName = userService.findUserById(chatId).getFirstName();
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getEDIT_USER_FIRSTNAME_SUCCESS_MESSAGE(), firstName) + messages.getFURTHER_ACTION_MESSAGE());
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return sendMessage;
    }

//    Delete User

    public EditMessageText deleteUserStartProcessMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getDELETE_USER_START_MESSAGE());
        editMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getDELETE_TEXT(), buttons.getDELETE_USER_CONFIRM_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        return editMessage;
    }

    public EditMessageText deleteUserSuccessMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getDELETE_USER_DONE_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }

    public void deleteUser(long chatId) {
        User user = userService.findUserById(chatId);
        log.info("start deletion of User " + user + " and his cars");
        carHandler.deleteAllCars(chatId);
        userService.deleteUser(chatId);
    }

}
