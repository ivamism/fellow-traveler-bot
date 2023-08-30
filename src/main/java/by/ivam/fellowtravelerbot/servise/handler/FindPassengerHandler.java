package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.bot.enums.Day;
import by.ivam.fellowtravelerbot.bot.enums.FindPassengerOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.model.Direction;
import by.ivam.fellowtravelerbot.model.Location;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.storages.interfaces.FindPassengerStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// This class handle operations with search passengers
@Service
@Data
@Log4j
public class FindPassengerHandler extends Handler implements HandlerInterface {
    @Autowired
    FindPassengerStorageAccess findPassengerStorageAccess;
    @Autowired
    AdminHandler adminHandler;
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();


    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        log.debug("method handleReceivedMessage");
        String messageText = incomeMessage.getText();
        Long chatId = incomeMessage.getChatId();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus + ". message: " + messageText);
        String process = chatStatus;
        if (chatStatus.contains(":")) {
            process = trimProcess(chatStatus);
        }
        switch (process) {
            case "CREATE_REQUEST_TIME_STATUS" -> {
                LocalTime time = getTime(messageText, chatId);
                if (time.toNanoOfDay() == 100) {
                    sendMessage = createNewRequestInvalidTimeFormatMessage(chatId);
                } else {
                    createNewRequestSetTime(chatId, time);
                    sendMessage = nextStep(chatId);
                }

            }
        }
        sendBotMessage(sendMessage);

    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug("method handleReceivedCallback. get callback: " + callback);
        Long chatId = incomeMessage.getChatId();
        String process = callback;
        if (callback.contains(":")) {
            process = trimProcess(callback);
        }
        switch (process) {
            case "CREATE_FIND_PASSENGER_REQUEST_CALLBACK" -> {
                createFindPassengerRequestDTO(chatId);
                editMessage = createNewRequestChoseDirectionMessage(incomeMessage);
            }
            case "CREATE_REQUEST_DIRECTION" -> {
                String direction = trimSecondSubstring(callback);
                createNewRequestSetDirection(chatId, direction);
                if (direction.equals(String.valueOf(Direction.FROM_MINSK))) {
                    int settlementId = settlementService.findByName("Минск").getId();
                    createNewRequestSetDepartureSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDepartureLocationMessage(incomeMessage, settlementId);

                } else if ((direction.equals(String.valueOf(Direction.TOWARDS_MINSK)))) {
                    editMessage = createNewRequestChooseResidenceAsDepartureMessage(incomeMessage);
                }
            }
            case "CREATE_REQ_DEP_SETTLEMENT" -> {
                int settlementId = trimId(callback);
                createNewRequestSetDepartureSettlement(chatId, settlementId);
                editMessage = createNewRequestChooseDepartureLocationMessage(incomeMessage, settlementId);
            }
            case "CREATE_REQ_ANOTHER_SETTLEMENT" -> {
                editMessage = createNewRequestChooseAnotherSettlementAsDepartureMessage(incomeMessage);
            }
            case "CREATE_REQUEST_DEP_LOCATION" -> {
                createNewRequestSetDepartureLocation(chatId, trimId(callback));
                if (findPassengerStorageAccess.getDTO(chatId).getDirection().equals(String.valueOf(Direction.TOWARDS_MINSK))) {
                    int settlementId = settlementService.findByName("Минск").getId();
                    createNewRequestSetDestinationSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDestinationLocationMessage(incomeMessage, settlementId);

                } else if ((findPassengerStorageAccess.getDTO(chatId).getDirection().equals(String.valueOf(Direction.FROM_MINSK)))) {
                    editMessage = createNewRequestChooseResidenceAsDestinationMessage(incomeMessage);
                }
            }
            case "CREATE_REQ_DEST_SETTLEMENT" -> {
                int settlementId = trimId(callback);
                createNewRequestSetDestinationSettlement(chatId, settlementId);
                editMessage = createNewRequestChooseDestinationLocationMessage(incomeMessage, settlementId);
            }
            case "CREATE_REQ_ANOTHER_DEST_SETTLEMENT" -> {
                editMessage = createNewRequestChooseAnotherSettlementAsDestinationMessage(incomeMessage);
            }
            case "CREATE_REQUEST_DEST_LOCATION" -> {
                createNewRequestSetDestinationLocation(chatId, trimId(callback));
                editMessage = createNewRequestChooseDateMessage(incomeMessage);
            }
            case "CREATE_REQUEST_DATE" -> {
                String day = trimSecondSubstring(callback);
                createNewRequestSetDate(chatId, day);
                if (isToday(day)) editMessage = createNewRequestChooseTimeTodayMessage(incomeMessage);
                else editMessage = createNewRequestChooseTimeTomorrowMessage(incomeMessage);
            }

        }
        sendEditMessage(editMessage);
    }

    public void startCreateNewRequest(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_START_PROCESS_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_FIND_PASSENGER_REQUEST_CALLBACK)); // Add car button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        log.debug("method: startCreateNewRequest");
        sendBotMessage(sendMessage);
    }

    private void createFindPassengerRequestDTO(long chatId) {
        FindPassengerRequestDTO findPassengerRequestDTO = new FindPassengerRequestDTO();
        findPassengerRequestDTO.setUser(userService.findUserById(chatId));
        findPassengerStorageAccess.addPickUpPassengerDTO(chatId, findPassengerRequestDTO);
        log.debug("method: createFindPassengerRequestDTO - create DTO " + findPassengerRequestDTO + " and save it in storage");
    }

    private EditMessageText createNewRequestChoseDirectionMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DIRECTION_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)

        buttonsAttributesList.add(buttons.towardMinskButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.TOWARDS_MINSK)); // toward Minsk button
        buttonsAttributesList.add(buttons.fromMinskButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.FROM_MINSK)); // from Minsk button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));

        log.debug("method: createNewRequestChoseDirectionMessage");

        return editMessage;
    }

    private void createNewRequestSetDirection(long chatId, String direction) {
        log.debug("method createNewRequestSetDirection");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDirection(direction);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText createNewRequestChooseResidenceAsDepartureMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SETTLEMENT_MESSAGE());
        Settlement settlement = userService.findUserById(incomeMessage.getChatId()).getResidence();

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.buttonCreate(settlement.getName(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_SETTLEMENT_CALLBACK.getValue() + settlement.getId()));
        buttonsAttributesList.add(buttons.anotherButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_ANOTHER_SETTLEMENT_CALLBACK.getValue()));
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChooseResidenceAsDepartureMessage");

        return editMessage;
    }

    private EditMessageText createNewRequestChooseAnotherSettlementAsDepartureMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SETTLEMENT_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_SETTLEMENT_CALLBACK.getValue();

        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callbackData, settlementList);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        log.debug("method: createNewRequestChooseAnotherSettlementAsDepartureMessage");
        return editMessage;
    }

    private void createNewRequestSetDepartureSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDepartureSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDepartureSettlement(settlement);
        findPassengerStorageAccess.update(chatId, dto);
//        findPassengerStorageAccess.setDepartureSettlement(chatId, settlementService.findByName(settlementName));
    }

    private EditMessageText createNewRequestChooseDepartureLocationMessage(Message incomeMessage, int settlementId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DEPARTURE_LOCATION_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DEP_LOCATION_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.departureLocationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChooseDepartureLocationMessage");

        return editMessage;
    }

    private void createNewRequestSetDepartureLocation(long chatId, int locationId) {
        log.debug("method createNewRequestSetDepartureLocation");
        Location location = locationService.findById(locationId);
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDepartureLocation(location);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private void createNewRequestSetDestinationSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDestinationSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDestinationSettlement(settlement);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText createNewRequestChooseResidenceAsDestinationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DESTINATION_SETTLEMENT_MESSAGE());
        Settlement settlement = userService.findUserById(incomeMessage.getChatId()).getResidence();

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.buttonCreate(settlement.getName(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DESTINATION_SETTLEMENT_CALLBACK.getValue() + settlement.getId()));
        buttonsAttributesList.add(buttons.anotherButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_ANOTHER_SETTLEMENT_CALLBACK.getValue()));
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChooseResidenceAsDestinationMessage");

        return editMessage;
    }

    private EditMessageText createNewRequestChooseAnotherSettlementAsDestinationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DESTINATION_SETTLEMENT_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DESTINATION_LOCATION_CALLBACK.getValue();

        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callbackData, settlementList);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        log.debug("method: createNewRequestChooseAnotherSettlementAsDestinationMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseDestinationLocationMessage(Message incomeMessage, int settlementId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DESTINATION_LOCATION_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DESTINATION_LOCATION_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.departureLocationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChooseDestinationLocationMessage");

        return editMessage;
    }

    private void createNewRequestSetDestinationLocation(long chatId, int locationId) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");
        Location location = locationService.findById(locationId);
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDestinationLocation(location);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText createNewRequestChooseDateMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DATE_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.todayButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TODAY)); // Today button
        buttonsAttributesList.add(buttons.tomorrowButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TOMORROW)); // Tomorrow button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChooseDateMessage");

        return editMessage;
    }

    private void createNewRequestSetDate(long chatId, String day) {
        log.debug("method createNewRequestSetDate");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId);
        LocalDate rideDate = LocalDate.now();
        if (isToday(day)) dto.setDepartureDate(rideDate);
        else dto.setDepartureDate(rideDate.plusDays(1));
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText createNewRequestChooseTimeTodayMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_TIME_STATUS);
        log.debug("method: createNewRequestChooseTimeTodayMessage");

        return editMessage;
    }

    private EditMessageText createNewRequestChooseTimeTomorrowMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_TIME_STATUS);
        log.debug("method: createNewRequestChooseTimeTomorrowMessage");

        return editMessage;
    }

    private void createNewRequestSetTime(long chatId, LocalTime time) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");

        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDepartureTime(time);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private boolean isToday(String day) {
        boolean isToday = false;
        if (day.equals(String.valueOf(Day.TODAY))) isToday = true;
        log.debug("method isToday = " + isToday);
        return isToday;
    }

    private LocalTime getTime(String timeString, long chatId) {
        LocalTime time = LocalTime.of(0, 0, 0, 100);

        if (timeString.contains(("."))) {
            DateTimeFormatter dotFormatter = DateTimeFormatter.ofPattern("H.m");
            time = parseTime(timeString, dotFormatter, chatId);
        } else if (timeString.contains((":"))) {
            DateTimeFormatter colonFormatter = DateTimeFormatter.ofPattern("H:m");
            time = parseTime(timeString, colonFormatter, chatId);
        } else if (timeString.contains(("-"))) {
            DateTimeFormatter dashFormatter = DateTimeFormatter.ofPattern("H-m");
            time = parseTime(timeString, dashFormatter, chatId);
        }
        log.debug("method getTime. time = " + time);
        return time;
    }

    private LocalTime parseTime(String timeString, DateTimeFormatter formatter, long chatId) {
        LocalTime time = LocalTime.of(0, 0, 0, 100);
        try {
            time = LocalTime.parse(timeString, formatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.debug("method parseTime. time = " + time);
        return time;
    }

    private SendMessage createNewRequestInvalidTimeFormatMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_INVALID_TIME_FORMAT_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        log.debug("method: createNewRequestInvalidTimeFormatMessage");
        return sendMessage;
    }

    private SendMessage nextStep(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText("nextStep");
        log.debug("method: nextStep");

        return sendMessage;
    }

    private void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }


}
