package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.enums.Day;
import by.ivam.fellowtravelerbot.bot.enums.Direction;
import by.ivam.fellowtravelerbot.bot.enums.FindPassengerRequestOperation;
import by.ivam.fellowtravelerbot.model.Settlement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Log4j2
@Data
public class RequestHandler extends MessageHandler {

    @Autowired
    protected AdminHandler adminHandler;
    @Autowired
    protected CarHandler carHandler;

    protected SendMessage sendMessage = new SendMessage();
    protected EditMessageText editMessage = new EditMessageText();


    protected SendMessage createNewRequest(long chatId, String messageText, String handlerPrefix) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and handlerPrefix)
        buttonsAttributesList.add(buttons.yesButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.CREATE_REQUEST_CALLBACK.getValue())); // Start create button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequest");
        return sendMessage;
    }

    protected EditMessageText createChoseDirectionMessage(Message incomeMessage, String handlerPrefix) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_REQUEST_DIRECTION_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.towardMinskButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.CREATE_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.TOWARDS_MINSK)); // toward Minsk button
        buttonsAttributesList.add(buttons.fromMinskButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.CREATE_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.FROM_MINSK)); // from Minsk button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChoseDirectionMessage");
        return editMessage;
    }

    protected EditMessageText createNecessityToCancelMessage(Message incomeMessage, String handlerPrefix) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_NECESSITY_TO_CANCEL_REQUEST_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and handlerPrefix)
        buttonsAttributesList.add(buttons.yesButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.CHOOSE_REQUEST_TO_CANCEL_CALLBACK.getValue())); // Edit date button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNecessityToCancelMessage");
        return editMessage;
    }

    protected EditMessageText createChooseResidenceMessage(Message incomeMessage, String messageText, String callback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        Settlement settlement = userService.findUserById(incomeMessage.getChatId()).getResidence();
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.buttonCreate(settlement.getName(), callback + settlement.getId()));
        buttonsAttributesList.add(buttons.anotherButtonCreate(callback + -1));
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createChooseResidenceMessage");
        return editMessage;
    }

    protected EditMessageText createChooseOfAllSettlementsMessage(Message incomeMessage, String messageText, String callback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callback, settlementService.findAll());
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDepartureSettlementMessage");
        return editMessage;
    }

    protected EditMessageText createChooseAnotherSettlementMessage(Message incomeMessage, List<Settlement> settlementList, String messageText, String callback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callback, settlementList);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createChooseAnotherSettlementAsMessage");
        return editMessage;
    }

    protected EditMessageText createChooseLocationMessage(Message incomeMessage, int settlementId, String messageText, String callback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.locationButtonsAttributesListCreator(callback, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: chooseDepartureLocationMessage");
        return editMessage;
    }

    protected EditMessageText createChooseDateMessage(Message incomeMessage, String todayCallback, String tomorrowCallback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_REQUEST_DATE_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.todayButtonCreate(todayCallback)); // Today button
        buttonsAttributesList.add(buttons.tomorrowButtonCreate(tomorrowCallback)); // Tomorrow button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createChooseDateMessage");
        return editMessage;
    }

    protected EditMessageText createDateTimeMessage(Message incomeMessage, String callbackDate, String callbackTime) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.dateButtonCreate(callbackDate)); // Edit date button
        buttonsAttributesList.add(buttons.timeButtonCreate(callbackTime)); // Edit time button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createDateTimeMessage");
        return editMessage;
    }

    protected void createTimeSendMessage(long chatId, String chatStatus) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusOperations.addChatStatus(chatId, chatStatus);
        log.debug("method: createTimeSendMessage");
        sendBotMessage(sendMessage);
    }

    protected EditMessageText createTimeMessage(Message incomeMessage, String messageText, String chatStatus) {
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusOperations.addChatStatus(incomeMessage.getChatId(), chatStatus);
        log.debug("method: createTimeMessage");
        return editMessage;
    }

    protected EditMessageText createCarDetailsMessage(Message incomeMessage, String carCallback, String seatsCallback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.carButtonCreate(carCallback)); // Change car button
        buttonsAttributesList.add(buttons.seatsQuantityButtonCreate(seatsCallback)); // Edit seats quantity button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createCarDetailsMessage");
        return editMessage;
    }

    protected EditMessageText createSeatsMessage(Message incomeMessage, String chatStatus) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SEATS_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusOperations.addChatStatus(incomeMessage.getChatId(), chatStatus);
        log.debug("method: createSeatsMessage");
        return editMessage;
    }

    protected SendMessage createSeatsMessage(long chatId, String chatStatus) {
        sendMessage.setText(messages.getCREATE_FIND_RIDE_REQUEST_SEATS_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusOperations.addChatStatus(chatId, chatStatus);
        log.debug("method: createSeatsMessage");
        return sendMessage;
    }

    protected SendMessage createCommentaryMessage(long chatId, String handlerPrefix) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.skipButtonCreate(handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_SKIP_COMMENT_CALLBACK)); // Skip step button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        chatStatusOperations.addChatStatus(chatId, handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_COMMENTARY_STATUS);
        log.debug("method: createCommentaryMessage");
        return sendMessage;
    }

    protected EditMessageText createCommentaryMessage(Message incomeMessage, String chatStatus) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusOperations.addChatStatus(incomeMessage.getChatId(), chatStatus);
        log.debug("method: createCommentaryMessage");
        return editMessage;
    }


    protected EditMessageText createCheckDataBeforeSaveMessageSkipComment(Message incomeMessage, String messageText, String handlerPrefix) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(createCheckBeforeSaveButtonsAttributesList(handlerPrefix)));
        log.debug("method checkDataBeforeSaveMessageSkipComment");
        return editMessage;
    }

    protected SendMessage createCheckDataBeforeSaveMessage(long chatId, String messageText, String handlerPrefix) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(createCheckBeforeSaveButtonsAttributesList(handlerPrefix)));
        log.debug("method createCheckDataBeforeSaveMessage");
        return sendMessage;
    }

    private List<Pair<String, String>> createCheckBeforeSaveButtonsAttributesList(String handlerPrefix) {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.saveButtonCreate(handlerPrefix + FindPassengerRequestOperation.SAVE_REQUEST_CALLBACK)); // Save button
        buttonsAttributesList.add(buttons.editButtonCreate(handlerPrefix + FindPassengerRequestOperation.EDIT_REQUEST_BEFORE_SAVE_CALLBACK)); // Edit button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    protected EditMessageText createRequestSaveSuccessMessage(Message incomeMessage, String requestToString) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getREQUEST_SAVE_SUCCESS_MESSAGE() + requestToString + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        log.debug("method createRequestSaveSuccessMessage");
        return editMessage;
    }

    protected EditMessageText createCancelRequestSuccessMessage(Message incomeMessage, String requestToString) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_CANCEL_REQUEST_SUCCESS_MESSAGE() + requestToString + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        log.debug("method createCancelRequestSuccessMessage");
        return editMessage;
    }

    protected EditMessageText createStartEditRequestMessage(Message incomeMessage, List<Pair<String, String>> buttonsAttributesList) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createStartEditRequestMessage");
        return editMessage;
    }

    //                TODO продумать изменения направления
    protected EditMessageText createEditSettlementLocationMessage(Message incomeMessage, List<Pair<String, String>> buttonsAttributesList) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createEditSettlementLocationMessage");
        return editMessage;
    }

    protected EditMessageText createEditSettlementMessage(Message incomeMessage, String messageText, String callbackData, int requestId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callbackData, settlementService.findAll(), requestId);
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: editDepartureSettlementMessage");
        return editMessage;
    }

    protected EditMessageText editLocationMessage(Message incomeMessage, String messageText, String callbackData, int settlementId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.locationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: editLocationMessage");
        return editMessage;
    }

    protected SendMessage handleReceivedIncorrectTime(LocalTime time, long chatId) {
        if (time.toNanoOfDay() == 100) {
            sendMessage = createNewRequestInvalidTimeFormatMessage(chatId);
        } else {
            sendMessage = createExpiredTimeMessage(chatId);
        }
        return sendMessage;
    }

    private SendMessage createNewRequestInvalidTimeFormatMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_INVALID_TIME_FORMAT_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        log.debug("method: createNewRequestInvalidTimeFormatMessage");
        return sendMessage;
    }

    private SendMessage createExpiredTimeMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_EXPIRED_TIME_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        log.debug("method: createNewRequestInvalidTimeFormatMessage");
        return sendMessage;
    }

    protected void expiredTimeMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_EXPIRED_TIME_MESSAGE2());
        sendMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        sendBotMessage(sendMessage);
        log.debug("method: expiredTimeMessage");
    }

    protected SendMessage invalidSeatsQuantityFormatMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SEATS_QUANTITY_INVALID_FORMAT_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        log.debug("method: createNewRequestInvalidTimeFormatMessage");
        return sendMessage;
    }

    protected EditMessageText noActiveRequestsMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_NO_ACTIVE_REQUEST_MESSAGE());
        log.info("noActiveRequestsMessage");
        editMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        return editMessage;
    }

    protected EditMessageText createChoiceRequestMessage(Message incomeMessage, String messageText, List<Pair<String, String>> buttonsAttributesList) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createChoiceRequestMessage");
        return editMessage;
    }

    protected SendMessage createExpireRequestTimeMessage(long chatId, String requestToString) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getTIME_EXPIRE_MESSAGE(), requestToString));
        sendMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        log.debug("method: sendExpireRequestTimeMessage");
        return sendMessage;
    }

    protected boolean isToday(String day) {
//        TODO добавить проверку day на совпадение со значениями enum Day
        boolean isToday = day.equals(String.valueOf(Day.TODAY));
        log.debug("method isToday = " + isToday);
        return isToday;
    }

    protected LocalTime getTime(String timeString) {
        LocalTime timeOnIncorrectTimeString = LocalTime.of(0, 0, 0, 100);
        String[] splitters = {".", ":", "-", ","};

        LocalTime time = Arrays.stream(splitters)
                .filter(splitter -> timeString.contains(splitter))
                .map(splitter -> DateTimeFormatter.ofPattern("H" + splitter + "m"))
                .map(formatter -> LocalTime.parse(timeString, formatter))
                .findFirst()
                .orElse(timeOnIncorrectTimeString);

        log.debug("method getTime. Time = " + time);
        return time;
    }

//    private LocalTime parseTime(String timeString, DateTimeFormatter formatter) {
//        LocalTime time = LocalTime.of(0, 0, 0, 100);
//        try {
//            time = LocalTime.parse(timeString, formatter);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        log.debug("method parseTime. time = " + time);
//        return time;
//    }

    protected boolean seatsQuantityIsValid(String s) {
        int maxSeatsQuantity = 4;
        return Character.isDigit(s.charAt(0)) && s.length() == 1 && (Integer.parseInt(s) > 0 & Integer.parseInt(s) <= maxSeatsQuantity);
    }

    protected void editMessageTextGeneralPreset(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
    }

    protected SendMessage nextStep(long chatId) {
//        TODO удалить метод по окончании реализации всего функционала
        sendMessage.setChatId(chatId);
        sendMessage.setText("nextStep");
        sendMessage.setReplyMarkup(null);
        log.debug("method: nextStep");
        return sendMessage;
    }

    protected EditMessageText nextStep(Message incomemessage) {
        //        TODO удалить метод по окончании реализации всего функционала
        editMessageTextGeneralPreset(incomemessage);
        editMessage.setText("nextStep");
        editMessage.setReplyMarkup(null);
        log.debug("method: nextStep");
        return editMessage;
    }

}
