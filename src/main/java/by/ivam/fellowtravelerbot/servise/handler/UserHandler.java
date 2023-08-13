package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.UserDTO;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.UserOperation;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.servise.SettlementService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.bot.enums.ChatStatus;
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
public class UserHandler implements Handler {
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
    @Autowired
    ResponseMessageProcessor messageProcessor;
    EditMessageText editMessage = new EditMessageText();
    SendMessage sendMessage = new SendMessage();

    /*  TODO Добавить сообщение о невозможности пользоваться ботом без регистрации и предложить вернутся к регистрации
TODO Реализовать процесс возврата к регистрации
TODO разделить функциональные действия и отправку сообщений
  Переделать онлайн клавиатуры на прием листа пар

     */

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        String messageText = incomeMessage.getText();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus);
        switch (chatStatus) {
            case "REGISTRATION_GET_EDITED_NAME" -> {
                log.info("Get edited name " + messageText);
                sendMessage = confirmEditedUserFirstName(incomeMessage);
            }
        }
        messageProcessor.sendMessage(sendMessage);
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String process = callback;
        if (callback.contains(":")){
           process = CommonMethods.trimProcess(callback);
        }

        log.debug("method handleReceivedCallback. get callback: " + callback);
        log.debug("process: " + process);
        switch (process) {
            case "START_REGISTRATION_CALLBACK" -> {
                editMessage = confirmUserFirstName(incomeMessage);
            }
            case "DENY_REGISTRATION_CALLBACK" -> {
                editMessage = denyRegistration(incomeMessage);
            }
            case "REQUEST_SETTLEMENT_CALLBACK" -> {
                editMessage = requestResidenceMessage(incomeMessage);
            }
            case "REGISTRATION_EDIT_NAME" -> {
                editMessage = editUserFirstNameBeforeSaving(incomeMessage);
            }
            case "SAVE_SETTLEMENT_CALLBACK" -> {
                setSettlementToDTO(chatId, callback);
                userRegistration(chatId);
                editMessage = userRegistrationSuccessMessage(incomeMessage);
            }
        }
        messageProcessor.sendEditedMessage(editMessage);
    }
    // Start registration User process

    public void startRegistration(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getSTART_REGISTRATION());

        Pair<String, String> yesButton = keyboards.buttonAttributesPairCreator(buttons.getYES_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.START_REGISTRATION_CALLBACK);
        Pair<String, String> cancelButton = keyboards.buttonAttributesPairCreator(buttons.getNO_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.DENY_REGISTRATION_CALLBACK);
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(yesButton);
        buttonsAttributesList.add(cancelButton);
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method startRegistration");
        messageProcessor.sendMessage(sendMessage);
    }

    // Ask user to confirm telegram User's first name as UserName or edit it
    private EditMessageText confirmUserFirstName(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);

        String firstName = incomeMessage.getChat().getFirstName();
//        editMessage.setChatId(incomeMessage.getChatId());
//        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getCONFIRM_USER_FIRST_MESSAGE() + firstName + "?");

        Pair<String, String> yesButton = keyboards.buttonAttributesPairCreator(buttons.getYES_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.REQUEST_SETTLEMENT_CALLBACK);
        Pair<String, String> editButton = keyboards.buttonAttributesPairCreator(buttons.getEDIT_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.REGISTRATION_EDIT_NAME);
        Pair<String, String> cancelButton = keyboards.buttonAttributesPairCreator(buttons.getNO_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.DENY_REGISTRATION_CALLBACK);
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(yesButton);
        buttonsAttributesList.add(editButton);
        buttonsAttributesList.add(cancelButton);
        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        userDTOCreator(incomeMessage);

        log.debug("method confirmUserFirstName");

        return editMessage;
    }

    //    Request to send users choice of userFirstName
    private EditMessageText editUserFirstNameBeforeSaving(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        long chatId = incomeMessage.getChatId();
//        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getEDIT_USER_FIRSTNAME_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        chatStatusStorageAccess.addChatStatus(chatId, Handlers.USER.getHandlerPrefix() + UserOperation.REGISTRATION_GET_EDITED_NAME);
//        chatStatusStorageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.REGISTRATION_USER_EDIT_NAME));
        log.debug("method editUserFirstNameBeforeSaving. Send request to enter User's firstname or nick");
        return editMessage;
    }

    //    request user to confirm that edited userFirstName is correct
    private SendMessage confirmEditedUserFirstName(Message incomeMessage) {

        long chatId = incomeMessage.getChatId();
        String incomeMessageText = incomeMessage.getText();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCONFIRM_FIRSTNAME_MESSAGE() + incomeMessage.getText());

        Pair<String, String> yesButton = keyboards.buttonAttributesPairCreator(buttons.getYES_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.REQUEST_SETTLEMENT_CALLBACK);
        Pair<String, String> editButton = keyboards.buttonAttributesPairCreator(buttons.getEDIT_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.REGISTRATION_EDIT_NAME);
        Pair<String, String> cancelButton = keyboards.buttonAttributesPairCreator(buttons.getNO_BUTTON_TEXT(),
                Handlers.USER.getHandlerPrefix() + UserOperation.DENY_REGISTRATION_CALLBACK);
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(yesButton);
        buttonsAttributesList.add(editButton);
        buttonsAttributesList.add(cancelButton);
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
//        sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getNAME_TO_CONFIRM_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_REG_DATA_CALLBACK()));
        userDTOCreator(incomeMessage, incomeMessageText);
        log.info("method confirmEditedUserFirstName. got user edited firstname and request to confirm edited name");
        return sendMessage;
    }

    private EditMessageText requestResidenceMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
//        long chatId = incomeMessage.getChatId();
//        editMessage.setChatId(chatId);
//        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE());
        String callback = Handlers.USER.getHandlerPrefix() + UserOperation.SAVE_SETTLEMENT_CALLBACK.getValue();
//TODO Изменить колбэк кнопки отмены
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(keyboards.settlementsButtonsAttributesListCreator(adminHandler.getSettlementsList(), callback)));
        log.debug("method requestResidence.");
        return editMessage;
    }

    private void setSettlementToDTO(long chatId, String callbackData) {
        userDTOStorageAccess.setResidence(chatId, settlementService.findById(CommonMethods.trimId(callbackData)));
        log.debug("method setResidenceToDTO");
    }

    // Save User to DB
    private EditMessageText userRegistrationSuccessMessage(Message incomeMessage) {
editMessageTextGeneralPreset(incomeMessage);
//        editMessage.setChatId(incomeMessage.getChatId());
//        editMessage.setMessageId(incomeMessage.getMessageId());
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

    public EditMessageText denyRegistration(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
//        editMessage.setMessageId(incomeMessage.getMessageId());
//        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setText(messages.getDENY_REGISTRATION_MESSAGE());
        log.info("User deny registration");
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }

//    Edit User's data

    private String getUserData(long chatId) {
        User user = userService.findUserById(chatId);
        return String.format(messages.getUSER_DATA(), user.getChatId(), user.getFirstName(), user.getUserName(), user.getResidence().getName()) + carHandler.prepareCarListToSend(chatId);
    }

    public void sendUserData(long chatId) {
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
        if (carHandler.getUsersCarsQuantity(chatId) == 0) {
            buttonsAttributesList.add(changeNameButton);
            buttonsAttributesList.add(changeResidenceButton);
            buttonsAttributesList.add(addCarButton);
            buttonsAttributesList.add(deleteUserButton);
            //        buttonsAttributesList.add(cancelButton);
        } else {
            buttonsAttributesList.add(changeNameButton);
            buttonsAttributesList.add(changeResidenceButton);
            if (carHandler.getUsersCarsQuantity(chatId) < 2) buttonsAttributesList.add(addCarButton);
            buttonsAttributesList.add(changeCarButton);
            buttonsAttributesList.add(deleteCarButton);
            buttonsAttributesList.add(deleteUserButton);
//        buttonsAttributesList.add(cancelButton);
        }

        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.info("send message with stored User's data and keyboard with further action menu");

        messageProcessor.sendMessage(sendMessage);
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
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(keyboards.settlementsButtonsAttributesListCreator(adminHandler.getSettlementsList(), buttons.getEDIT_USER_RESIDENCE_CALLBACK())));

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

    public void editMessageTextGeneralPreset(Message incomeMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }

}
