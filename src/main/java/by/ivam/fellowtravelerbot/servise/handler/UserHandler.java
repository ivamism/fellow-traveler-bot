package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.UserDTO;
import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.servise.SettlementService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.servise.handler.enums.ChatStatus;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import by.ivam.fellowtravelerbot.storages.interfaces.UserDTOStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;


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
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    UserDTOStorageAccess userDTOStorageAccess;
    @Autowired
    UserDTO userDTO;
    @Autowired
    AdminHandler adminHandler;
    @Autowired
    SettlementService settlementService;

    @Autowired
    CarHandler carHandler;
    EditMessageText editMessage = new EditMessageText();
    SendMessage sendMessage = new SendMessage();

    /*  TODO Добавить сообщение о невозможности пользоваться ботом без регистрации и предложить вернутся к регистрации
TODO Реализовать процесс возврата к регистрации
TODO разделить функциональные действия и отправку сообщений
  Переделать онлайн клавиатуры на прием листа пар

     */


    // Start registration User process
    public SendMessage startRegistration(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getSTART_REGISTRATION());
        sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(),
                buttons.getCONFIRM_START_REG_CALLBACK(),
                buttons.getNO_BUTTON_TEXT(),
                buttons.getDENY_REG_CALLBACK()));
        log.debug("method startRegistration");
        return sendMessage;
    }

    // Ask user to confirm telegram User's first name as UserName or edit it
    public EditMessageText confirmUserFirstName(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        String userName = incomeMessage.getChat().getUserName();
        String firstName = incomeMessage.getChat().getFirstName();
        editMessage.setChatId(chatId);
        editMessage.setText(messages.getCONFIRM_USER_FIRST_MESSAGE() + firstName + "?");
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setReplyMarkup(keyboards.threeButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getREG_USER_REQUEST_SETTLEMENT_CALLBACK(),
                buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK(),
                buttons.getCANCEL_BUTTON_TEXT(), buttons.getDENY_REG_CALLBACK()));
        userDTOCreator(incomeMessage);

        log.debug("method confirmUserFirstName");

        return editMessage;
    }

    //    Request to send users choice of userFirstName
    public EditMessageText editUserFirstNameBeforeSaving(Message incomeMessage) {

        long chatId = incomeMessage.getChatId();
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getEDIT_USER_FIRSTNAME_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        chatStatusStorageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.REGISTRATION_USER_EDIT_NAME));
        log.debug("method editUserFirstNameBeforeSaving. Send request to enter User's firstname or nick");
        return editMessage;
    }

    //    request user to confirm that edited userFirstName is correct
    public SendMessage confirmEditedUserFirstName(Message incomeMessage) {

        long chatId = incomeMessage.getChatId();
        String incomeMessageText = incomeMessage.getText();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCONFIRM_FIRSTNAME_MESSAGE() + incomeMessage.getText());
        sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getNAME_TO_CONFIRM_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK()));
        userDTOCreator(incomeMessage, incomeMessageText);
        log.info("method confirmEditedUserFirstName. got user edited firstname and request to confirm edited name");
        return sendMessage;
    }

    public EditMessageText requestResidenceMessage(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(keyboards.settlementsButtonsAttributesListCreator(adminHandler.getSettlementsList())));

        log.debug("method requestResidence.");
        return editMessage;
    }

    public void setResidenceToDTO(long chatId, String callbackData) {
        userDTOStorageAccess.setResidence(chatId, settlementService.findById(Integer.parseInt(callbackData.substring(32))));
        log.debug("method setResidenceToDTO");
    }

    // Save User to DB
    public EditMessageText userRegistrationSuccessMessage(Message incomeMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getSUCCESS_REGISTRATION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("method userRegistrationSuccessMessage.");
        return editMessage;
    }

    public User userRegistration(long chatId) {
        User user = userService.registerNewUser(userDTOStorageAccess.findUserDTO(chatId));

        chatStatusStorageAccess.deleteChatStatus(chatId);
        userDTOStorageAccess.deleteUserDTO(chatId);
        log.info("method userRegistration. Call saving to DB user: " + user);

        return user;
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
        return String.format(messages.getUSER_DATA(), user.getChatId(), user.getFirstName(), user.getUserName(), user.getResidence().getName()) + carHandler.prepareCarListToSend(chatId);
    }

    public SendMessage sendUserData(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(getUserData(chatId));
        //        Create pairs of buttons attributes and add them to list
        Pair<String, String> changeNameButton = keyboards.buttonAttributesPairCreator(buttons.getCHANGE_NAME_TEXT(),
                buttons.getEDIT_USER_NAME_CALLBACK());
        Pair<String, String> changeResidenceButton = keyboards.buttonAttributesPairCreator(buttons.getCHANGE_RESIDENCE_TEXT(),
                buttons.getEDIT_USER_RESIDENCE_CALLBACK());
        Pair<String, String> changeCarButton = keyboards.buttonAttributesPairCreator(buttons.getCHANGE_CAR_TEXT(),
                buttons.getEDIT_CAR_START_PROCESS_CALLBACK());
        Pair<String, String> addCarButton = keyboards.buttonAttributesPairCreator(buttons.getADD_CAR_TEXT(),
                buttons.getADD_CAR_CALLBACK());
        Pair<String, String> deleteCarButton = keyboards.buttonAttributesPairCreator(buttons.getDELETE_CAR_TEXT(),
                buttons.getREQUEST_DELETE_CAR_CALLBACK());
        Pair<String, String> deleteUserButton = keyboards.buttonAttributesPairCreator(buttons.getDELETE_ALL_TEXT(),
                buttons.getDELETE_USER_START_PROCESS_CALLBACK());
//        Pair<String, String> cancelButton = keyboards.buttonAttributesPairCreator(buttons.getCANCEL_BUTTON_TEXT(),
//                buttons.getCANCEL_CALLBACK());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
if (carHandler.getUsersCarsQuantity(chatId)==0){
    buttonsAttributesList.add(changeNameButton);
    buttonsAttributesList.add(changeResidenceButton);
    buttonsAttributesList.add(addCarButton);
    buttonsAttributesList.add(deleteUserButton);
    //        buttonsAttributesList.add(cancelButton);
} else {
    buttonsAttributesList.add(changeNameButton);
    buttonsAttributesList.add(changeResidenceButton);
    if (carHandler.getUsersCarsQuantity(chatId)<2) buttonsAttributesList.add(addCarButton);
    buttonsAttributesList.add(changeCarButton);
    buttonsAttributesList.add(deleteCarButton);
    buttonsAttributesList.add(deleteUserButton);
//        buttonsAttributesList.add(cancelButton);
}

        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
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
        chatStatusStorageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.USER_EDIT_NAME));
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

    public EditMessageText editUserResidenceRequestMessage(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(keyboards.editSettlementsButtonsAttributesCreator(adminHandler.getSettlementsList())));

        log.debug("method editUserResidenceRequestMessage");
        return editMessage;
    }

    public User editUserSetResidence(long chatId, String callbackData) {
        User user = userService.findUserById(chatId);
        user.setResidence(adminHandler.getSettlementService().findById(Integer.parseInt(callbackData.substring(28))));
        return userService.updateUser(user);
    }

    public EditMessageText editUserResidenceSuccessMessage(long chatId, User user) {

        editMessage.setChatId(chatId);
        editMessage.setText(String.format(messages.getEDIT_USER_RESIDENCE_SUCCESS_MESSAGE(), user.getResidence().getName()) + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
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

    private UserDTO userDTOCreator(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        userDTO.setChatId(chatId)
                .setFirstName(incomeMessage.getChat().getFirstName())
                .setTelegramUserName(incomeMessage.getChat().getUserName());
        userDTOStorageAccess.addUserDTO(chatId, userDTO);
        log.debug("method userDTOCreator");

        return userDTO;
    }

    private UserDTO userDTOCreator(Message incomeMessage, String firstname) {
        Long chatId = incomeMessage.getChatId();
        userDTO.setChatId(chatId)
                .setFirstName(firstname)
                .setTelegramUserName(incomeMessage.getChat().getUserName());
        userDTOStorageAccess.addUserDTO(chatId, userDTO);
        log.debug("method userDTOCreator with edited firstname");
        return userDTO;
    }
}
