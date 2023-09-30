package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.enums.Day;
import by.ivam.fellowtravelerbot.bot.enums.Direction;
import by.ivam.fellowtravelerbot.bot.enums.requestOperation;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.Settlement;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Log4j
public class RequestHandler extends Handler {

    @Autowired
    AdminHandler adminHandler;
    @Autowired
    CarHandler carHandler;
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    protected SendMessage createNewRequest(long chatId, String messageText, String handlerPrefix) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and handlerPrefix)
        buttonsAttributesList.add(buttons.yesButtonCreate(handlerPrefix + requestOperation.CREATE_REQUEST_CALLBACK.getValue())); // Start create button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequest");
        return sendMessage;
    }

    protected EditMessageText createChoseDirectionMessage(Message incomeMessage, String handlerPrefix) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_REQUEST_DIRECTION_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.towardMinskButtonCreate(handlerPrefix + requestOperation.CREATE_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.TOWARDS_MINSK)); // toward Minsk button
        buttonsAttributesList.add(buttons.fromMinskButtonCreate(handlerPrefix + requestOperation.CREATE_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.FROM_MINSK)); // from Minsk button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChoseDirectionMessage");
        return editMessage;
    }

    protected EditMessageText createNecessityToCancelMessage(Message incomeMessage, String handlerPrefix) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_NECESSITY_TO_CANCEL_REQUEST_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and handlerPrefix)
        buttonsAttributesList.add(buttons.yesButtonCreate(handlerPrefix + requestOperation.CHOOSE_REQUEST_TO_CANCEL_CALLBACK.getValue())); // Edit date button
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
        chatStatusStorageAccess.addChatStatus(chatId, chatStatus);
        log.debug("method: createTimeSendMessage");
        sendBotMessage(sendMessage);
    }

    protected EditMessageText createTimeMessage(Message incomeMessage, String messageText, String chatStatus) {
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), chatStatus);
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
        editMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), chatStatus);
        log.debug("method: createSeatsMessage");
        return editMessage;
    }

    protected EditMessageText createCommentaryMessage(Message incomeMessage, String chatStatus) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), chatStatus);
        log.debug("method: createCommentaryMessage");
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
            sendMessage = createNewRequestExpiredTimeMessage(chatId);
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

    private SendMessage createNewRequestExpiredTimeMessage(long chatId) {
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
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }


    protected boolean isToday(String day) {
//        TODO добавить проверку day на совпадение со значениями enum Day
        boolean isToday = day.equals(String.valueOf(Day.TODAY));
        log.debug("method isToday = " + isToday);
        return isToday;
    }

    protected LocalTime getTime(String timeString) {
        LocalTime time = LocalTime.of(0, 0, 0, 100);

        if (timeString.contains(("."))) {
            DateTimeFormatter dotFormatter = DateTimeFormatter.ofPattern("H.m");
            time = parseTime(timeString, dotFormatter);
        } else if (timeString.contains((":"))) {
            DateTimeFormatter colonFormatter = DateTimeFormatter.ofPattern("H:m");
            time = parseTime(timeString, colonFormatter);
        } else if (timeString.contains(("-"))) {
            DateTimeFormatter dashFormatter = DateTimeFormatter.ofPattern("H-m");
            time = parseTime(timeString, dashFormatter);
        }
        log.debug("method getTime. time = " + time);
        return time;
    }

    private LocalTime parseTime(String timeString, DateTimeFormatter formatter) {
        LocalTime time = LocalTime.of(0, 0, 0, 100);
        try {
            time = LocalTime.parse(timeString, formatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.debug("method parseTime. time = " + time);
        return time;
    }

    protected boolean seatsQuantityIsValid(String s) {
        return Character.isDigit(s.charAt(0)) && s.length() == 1 && (Integer.parseInt(s) > 0 & Integer.parseInt(s) < 5);
    }

    protected void editMessageTextGeneralPreset(Message incomeMessage) {
        long chatId =incomeMessage.getChatId();
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
