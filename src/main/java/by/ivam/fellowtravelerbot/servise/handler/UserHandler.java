package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.UserDTO;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.UserOperation;
import by.ivam.fellowtravelerbot.model.User;
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
This class handle operations with User registration process, editing, deleting, show info and saving User to DB
 */


@Service
@Data
@Log4j
public class UserHandler extends Handler implements HandlerInterface {
    @Autowired
    UserDTOStorageAccess userDTOStorageAccess;
    @Autowired
    UserDTO userDTO;
    @Autowired
    AdminHandler adminHandler;
    @Autowired
    CarHandler carHandler;
    @Autowired
    FindPassengerHandler findPassengerHandler;
    @Autowired
    FindRideHandler findRideHandler;
    EditMessageText editMessage = new EditMessageText();
    SendMessage sendMessage = new SendMessage();

    /*  TODO Добавить сообщение о невозможности пользоваться ботом без регистрации и предложить вернутся к регистрации
TODO разделить функциональные действия и отправку сообщений
     */

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        String messageText = incomeMessage.getText();
        Long chatId = incomeMessage.getChatId();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus);
        switch (chatStatus) {
            case "REGISTRATION_GET_EDITED_NAME_CHAT_STATUS" -> {
                log.info("Get edited name " + messageText);
                sendMessage = confirmEditedUserFirstName(incomeMessage);
            }
            case "EDIT_NAME_CHAT_STATUS" -> {
                log.info("Get edited name " + messageText);
                saveEditedUserFirstName(chatId, messageText);
                sendMessage = editUserFirstNameSuccessMessage(chatId);
            }
        }
        sendBotMessage(sendMessage);
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String process = callback;
        if (callback.contains(":")) {
            process = trimProcess(callback);
        }

        log.debug("method handleReceivedCallback. get callback: " + callback);
        log.debug("process: " + process);
        switch (process) {
            case "START_REGISTRATION_CALLBACK" -> editMessage = confirmUserFirstName(incomeMessage);
            case "DENY_REGISTRATION_CALLBACK" -> editMessage = denyRegistration(incomeMessage);
            case "REQUEST_SETTLEMENT_CALLBACK" -> editMessage = requestResidenceMessage(incomeMessage);
            case "REGISTRATION_EDIT_NAME" -> editMessage = editUserFirstNameBeforeSaving(incomeMessage);
            case "SAVE_SETTLEMENT_CALLBACK" -> {
                setSettlementToDTO(chatId, callback);
                userRegistration(chatId);
                editMessage = userRegistrationSuccessMessage(incomeMessage);
            }
            case "EDIT_NAME_CALLBACK" -> editMessage = editUserFirstNameMessage(incomeMessage);
            case "CHANGE_SETTLEMENT_REQUEST_CALLBACK" -> editMessage = editUserResidenceRequestMessage(incomeMessage);
            case "CHANGE_SETTLEMENT_CALLBACK" -> {
                User user = editUserSetResidence(chatId, callback);
                editMessage = editUserResidenceSuccessMessage(incomeMessage, user);
            }
            case "DELETE_USER" -> editMessage = deleteUserStartProcessMessage(incomeMessage);
            case "CONFIRM_USER_DELETION" -> {
                deleteUser(chatId);
                editMessage = deleteUserSuccessMessage(incomeMessage);
            }
            case "MY_RIDES_MENU" -> editMessage = showUserActiveRequestsListMessage(incomeMessage);

        }
        sendEditMessage(editMessage);
    }

    // Start registration User process
    public void startRegistration(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getSTART_REGISTRATION());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.START_REGISTRATION_CALLBACK));
        buttonsAttributesList.add(buttons.noButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.DENY_REGISTRATION_CALLBACK));
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method startRegistration");
        sendBotMessage(sendMessage);
    }

    // Ask user to confirm telegram User's first name as UserName or edit it
    private EditMessageText confirmUserFirstName(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        String firstName = incomeMessage.getChat().getFirstName();
        editMessage.setText(messages.getCONFIRM_USER_FIRST_MESSAGE() + firstName + "?");

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.REQUEST_SETTLEMENT_CALLBACK));
        buttonsAttributesList.add(buttons.editButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.REGISTRATION_EDIT_NAME));
        buttonsAttributesList.add(buttons.cancelButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.DENY_REGISTRATION_CALLBACK));
        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        userDTOCreator(incomeMessage);
        log.debug("method confirmUserFirstName");
        return editMessage;
    }

    //    Request to send users choice of userFirstName
    private EditMessageText editUserFirstNameBeforeSaving(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        long chatId = incomeMessage.getChatId();
        editMessage.setText(messages.getEDIT_USER_FIRSTNAME_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        chatStatusStorageAccess.addChatStatus(chatId, Handlers.USER.getHandlerPrefix() + UserOperation.REGISTRATION_GET_EDITED_NAME_CHAT_STATUS);
        log.debug("method editUserFirstNameBeforeSaving. Send request to enter User's firstname or nick");
        return editMessage;
    }

    //    request user to confirm that edited userFirstName is correct
    private SendMessage confirmEditedUserFirstName(Message incomeMessage) {

        String incomeMessageText = incomeMessage.getText();
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getCONFIRM_FIRSTNAME_MESSAGE() + incomeMessage.getText());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.REQUEST_SETTLEMENT_CALLBACK)); // Yes button
        buttonsAttributesList.add(buttons.editButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.REGISTRATION_EDIT_NAME)); // Edit button
        buttonsAttributesList.add(buttons.cancelButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.DENY_REGISTRATION_CALLBACK)); // No button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        userDTOCreator(incomeMessage, incomeMessageText);
        log.info("method confirmEditedUserFirstName. got user edited firstname and request to confirm edited name");
        return sendMessage;
    }

    private EditMessageText requestResidenceMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(Handlers.USER.getHandlerPrefix() + UserOperation.SAVE_SETTLEMENT_CALLBACK.getValue()); // List of buttons of Settlements
        buttonsAttributesList.add(buttons.cancelButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.DENY_REGISTRATION_CALLBACK)); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method requestResidence.");
        return editMessage;
    }

    private void setSettlementToDTO(long chatId, String callbackData) {
        userDTOStorageAccess.setResidence(chatId, settlementService.findById(trimId(callbackData)));
        log.debug("method setResidenceToDTO");
    }

    // Save User to DB
    private EditMessageText userRegistrationSuccessMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);

        editMessage.setText(messages.getSUCCESS_REGISTRATION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("method userRegistrationSuccessMessage.");
        return editMessage;
    }

    private User userRegistration(long chatId) {
        User user = userService.registerNewUser(userDTOStorageAccess.findUserDTO(chatId));
        chatStatusStorageAccess.deleteChatStatus(chatId);
        userDTOStorageAccess.deleteUserDTO(chatId);

        log.info("method userRegistration. Call saving to DB user: " + user);
        return user;
    }

    private EditMessageText denyRegistration(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);

        editMessage.setText(messages.getDENY_REGISTRATION_MESSAGE());
        log.info("User deny registration");
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }

//    Edit User's data

    private String getUserData(long chatId) {
        User user = userService.findUserById(chatId);
        return String.format(messages.getUSER_DATA(),
                user.getChatId(),
                user.getFirstName(),
                user.getUserName(),
                user.getResidence().getName())
                + carHandler.CarListToString(chatId);
    }

    public void userDataToString(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(getUserData(chatId));

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        if (carHandler.getUsersCarsQuantity(chatId) == 0) {
            buttonsAttributesList.add(buttons.changeNameButtonCreate()); // Change User's name button
            buttonsAttributesList.add(buttons.changeResidenceButtonCreate()); // Change User's residence settlement button
            buttonsAttributesList.add(buttons.addCarButtonCreate()); // Add a car button
            buttonsAttributesList.add(buttons.myRidesButtonCreate()); // Rides menu button
            buttonsAttributesList.add(buttons.deleteButtonCreate()); // Delete User button
        } else {
            buttonsAttributesList.add(buttons.changeNameButtonCreate()); // Change User's name button
            buttonsAttributesList.add(buttons.changeResidenceButtonCreate()); // Change User's residence settlement button
            buttonsAttributesList.add(buttons.myRidesButtonCreate()); // Rides menu button
            if (carHandler.getUsersCarsQuantity(chatId) < 2)
                buttonsAttributesList.add(buttons.addCarButtonCreate()); // Add a car button
            buttonsAttributesList.add(buttons.editCarButtonCreate()); // Edit User's cars button
            buttonsAttributesList.add(buttons.deleteCarButtonCreate()); // Delete User's cars button
            buttonsAttributesList.add(buttons.deleteButtonCreate()); // Delete User button
        }

        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.info("send message with stored User's data and keyboard with further action menu");
        sendBotMessage(sendMessage);
    }

    private EditMessageText editUserFirstNameMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        long chatId = incomeMessage.getChatId();
        String firstName = userService.findUserById(chatId).getFirstName();
        editMessage.setText(messages.getEDIT_USER_FIRSTNAME_MESSAGE() + String.format(messages.getEDIT_USER_FIRSTNAME_MESSAGE_POSTFIX(), firstName));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(chatId, Handlers.USER.getHandlerPrefix() + UserOperation.EDIT_NAME_CHAT_STATUS);
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
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(Handlers.USER.getHandlerPrefix() + UserOperation.CHANGE_SETTLEMENT_CALLBACK.getValue());
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        log.debug("method editUserResidenceRequestMessage");
        return editMessage;
    }

    public User editUserSetResidence(long chatId, String callbackData) {
        User user = userService.findUserById(chatId);
        user.setResidence(settlementService.findById(trimId(callbackData)));
        return userService.updateUser(user);
    }

    public EditMessageText editUserResidenceSuccessMessage(Message incomeMessage, User user) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(String.format(messages.getEDIT_USER_RESIDENCE_SUCCESS_MESSAGE(), user.getResidence().getName()) + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }

//    Delete User

    public EditMessageText deleteUserStartProcessMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getDELETE_USER_START_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.deleteButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.CONFIRM_USER_DELETION)); // Delete User button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        return editMessage;
    }

    public EditMessageText showUserActiveRequestsListMessage(Message incomeMessage) {
//        TODO после реализации FindRideHandler добавить проверку какой из реквестов последний и редактирование последнего
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(findPassengerHandler.requestListToString(incomeMessage.getChatId()));

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
//        buttonsAttributesList.add(buttons.deleteButtonCreate(Handlers.USER.getHandlerPrefix() + UserOperation.CONFIRM_USER_DELETION)); // Delete User button
//        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
//        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        return editMessage;
    }

    public EditMessageText deleteUserSuccessMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);

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

    private void userDTOCreator(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        userDTO.setChatId(chatId)
                .setFirstName(incomeMessage.getChat().getFirstName())
                .setTelegramUserName(incomeMessage.getChat().getUserName());
        userDTOStorageAccess.addUserDTO(chatId, userDTO);
        log.debug("method userDTOCreator");
    }

    private void userDTOCreator(Message incomeMessage, String firstname) {
        Long chatId = incomeMessage.getChatId();
        userDTO.setChatId(chatId)
                .setFirstName(firstname)
                .setTelegramUserName(incomeMessage.getChat().getUserName());
        userDTOStorageAccess.addUserDTO(chatId, userDTO);
        log.debug("method userDTOCreator with edited firstname");
    }

    public void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }
}
