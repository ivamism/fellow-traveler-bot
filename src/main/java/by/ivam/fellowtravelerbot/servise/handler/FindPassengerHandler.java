package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.bot.enums.Day;
import by.ivam.fellowtravelerbot.bot.enums.Direction;
import by.ivam.fellowtravelerbot.bot.enums.FindPassengerOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.model.*;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// This class handle operations with search passengers
@Service
@Data
@Log4j
public class FindPassengerHandler extends Handler implements HandlerInterface {
    @Autowired
    FindPassengerStorageAccess findPassengerStorageAccess;
    @Autowired
    AdminHandler adminHandler;
    @Autowired
    CarHandler carHandler;

    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    /*
         TODO
          добавить перед началом проверку на наличие  у юзера автомобиля
          ограничить возможное количество активных запросов и добавить соответсствующую проверку
      */
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
                LocalTime time = getTime(messageText);
                if (time.toNanoOfDay() == 100 || isExpired(chatId, time)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    createNewRequestSetTime(chatId, time);
                    sendMessage = createNewRequestChooseCarMessage(chatId);
                }
            }
            case "CREATE_REQUEST_SEATS_STATUS" -> {
                if (seatsQuantityIsValid(messageText)) {
                    createNewRequestSetSeatsQuantity(chatId, Integer.parseInt(messageText));
                    sendMessage = createNewRequestCommentaryMessage(incomeMessage);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "CREATE_REQUEST_COMMENTARY_STATUS" -> {
                createNewRequestSetCommentary(chatId, messageText);
                if (messageText.length() >= 1000) sendMessage = nextStep(chatId);
                else sendMessage = checkDataBeforeSaveMessage(incomeMessage);
//                TODO добавитиь сообщение если коментарий слишком длинный
            }
            case "EDIT_BEFORE_SAVE_CHANGE_TIME" -> {
                LocalTime time = getTime(messageText);
                if (time.toNanoOfDay() == 100 || isExpired(chatId, time)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    createNewRequestSetTime(chatId, time);
                    sendMessage = checkDataBeforeSaveMessage(incomeMessage);
                }
            }
            case "EDIT_BEFORE_SAVE_CHANGE_SEATS_QUANTITY_STATUS" -> {
                if (seatsQuantityIsValid(messageText)) {
                    createNewRequestSetSeatsQuantity(chatId, Integer.parseInt(messageText));
                    sendMessage = checkDataBeforeSaveMessage(incomeMessage);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "EDIT_CHANGE_TIME" -> {
                LocalTime time = getTime(messageText);
                int requestId = trimId(chatStatus);
                if (time.toNanoOfDay() == 100 || isExpired(requestId, time)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    FindPassengerRequest request = editSetTime(requestId, time);
                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                }
            }
            case "EDIT_CHANGE_SEATS_QUANTITY" -> {
                if (seatsQuantityIsValid(messageText)) {
                    FindPassengerRequest request = setEditedSeatsQuantity(trimId(chatStatus), Integer.parseInt(messageText));
                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "EDIT_CHANGE_COMMENTARY" -> {
                FindPassengerRequest request = setEditedCommentary(trimId(chatStatus), messageText);
                if (messageText.length() >= 1000) sendMessage = nextStep(chatId);
                else sendMessage = sendMessage = editRequestSuccessSendMessage(chatId, request);
//                TODO добавитиь сообщение если коментарий слишком длинный
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
                if (isRequestQuantityLimit(chatId)) editMessage = nextStep(incomeMessage);
                 else {
                    createFindPassengerRequestDTO(chatId);
                    editMessage = createNewRequestChoseDirectionMessage(incomeMessage);
                }
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
                editMessage = createNewRequestTimeMessage(incomeMessage);
            }
            case "CREATE_REQUEST_CAR_CALLBACK" -> {
                createNewRequestSetCar(chatId, trimId(callback));
                editMessage = createNewRequestSeatsMessage(incomeMessage);
            }
            case "CREATE_REQUEST_SKIP_COMMENT_CALLBACK" -> {
                createNewRequestSetCommentary(chatId, "-");
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "SAVE_REQUEST_CALLBACK" -> {
                FindPassengerRequest request = saveRequest(chatId);
                editMessage = saveRequestSuccessMessage(incomeMessage, request);
            }
            case "EDIT_BEFORE_SAVE_REQUEST_CALLBACK" -> {
                editMessage = startEditBeforeSaveRequestMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_SETTLEMENT_LOCATION" -> {
                editMessage = editBeforeSaveSettlementLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DATE_TIME" -> {
                editMessage = editBeforeSaveDateTimeMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CAR_DETAILS" -> {
                editMessage = editBeforeSaveCarDetailsMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEP_SETTLEMENT" -> {
                editMessage = editBeforeSaveDepartureSettlementMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEP_SETTLEMENT" -> {
                createNewRequestSetDepartureSettlement(chatId, trimId(callback));
                editMessage = editBeforeSaveDepartureLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEP_LOCATION" -> {
                editMessage = editBeforeSaveDepartureLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEP_LOCATION" -> {
                createNewRequestSetDepartureLocation(chatId, trimId(callback));
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEST_SETTLEMENT" -> {
                editMessage = editBeforeSaveDestinationSettlementMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEST_SETTLEMENT" -> {
                createNewRequestSetDestinationSettlement(chatId, trimId(callback));
                editMessage = editBeforeSaveDestinationLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEST_LOCATION" -> {
                editMessage = editBeforeSaveDestinationLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEST_LOCATION" -> {
                createNewRequestSetDestinationLocation(chatId, trimId(callback));
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_SWAP_DEP_DEST" -> {
                editBeforeSaveSwapDepartureDestination(chatId);
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DATE" -> {
                editMessage = editBeforeSaveChangeDateMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DATE" -> {
                String day = trimSecondSubstring(callback);
                createNewRequestSetDate(chatId, day);
                if (isToday(day) && isExpired(chatId)) {
                    expiredTimeMessage(chatId);
                    editBeforeSaveTimeSendMessage(chatId);
                } else editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_TIME" -> {
                editMessage = editBeforeSaveTimeMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CAR" -> {
                editMessage = editBeforeSaveChooseCarMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_CAR" -> {
                createNewRequestSetCar(chatId, trimId(callback));
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_SEATS_QUANTITY" -> {
                editMessage = editBeforeSaveSeatsMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_COMMENTARY_CALLBACK" -> {
                editMessage = editBeforeSaveCommentaryMessage(incomeMessage);
            }
            case "CHOOSE_REQUEST_TO_EDIT" -> {
                editMessage = chooseRequestToEditMessage(incomeMessage);
            }
            case "EDIT_REQUEST_START" -> {
                editMessage = startEditRequestMessage(incomeMessage, trimId(callback));
            }
            case "NO_ACTIVE_REQUEST" -> {
                editMessage = noActiveRequestsMessage(incomeMessage);
            }
            case "EDIT_SETTLEMENT_LOCATION" -> {
                editMessage = editSettlementLocationMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_SWAP_DEP_DEST" -> {
                FindPassengerRequest request = editSwapDepartureDestination(trimId(callback));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DEP_SETTLEMENT" -> {
                editMessage = editDepartureSettlementMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CHANGE_DEP_SETTLEMENT" -> {
                setEditedDepartureSettlement(trimId(callback), trimSecondId(callback));
                editMessage = editDepartureLocationMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_DEST_SETTLEMENT" -> {
                editMessage = editDestinationSettlementMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CHANGE_DEST_SETTLEMENT" -> {
                setEditedDestinationSettlement(trimId(callback), trimSecondId(callback));
                editMessage = editDestinationLocationMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_DEP_LOCATION" -> {
                editMessage = editDepartureLocationMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CHANGE_DEP_LOCATION" -> {
                FindPassengerRequest request = setEditedDepartureLocation(trimId(callback), trimSecondId(callback));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DEST_LOCATION" -> {
                editMessage = editDestinationLocationMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CHANGE_DEST_LOCATION" -> {
                FindPassengerRequest request = setEditedDestinationLocation(trimId(callback), trimSecondId(callback));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DATE_TIME" -> {
                editMessage = editDateTimeMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_DATE" -> {
                editMessage = editDateMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CHANGE_DATE" -> {
                String day = trimSecondSubstring(callback);
                int requestId = trimSecondId(callback);
                FindPassengerRequest request = editSetDate(trimSecondId(callback), trimSecondSubstring(callback));
                if (isExpired(requestId)) {
                    expiredTimeMessage(chatId);
                    editTimeSendValidTimeMessage(chatId);
                } else {
                    editMessage = editRequestSuccessEditMessage(incomeMessage, request);
                }
            }
            case "EDIT_TIME" -> {
                editMessage = editTimeMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CAR_DETAILS" -> {
                editMessage = editCarDetailsMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CAR" -> {
                editMessage = editChooseCarMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_CHANGE_CAR" -> {
                FindPassengerRequest request = setEditedCar(trimId(callback), trimSecondId(callback));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_SEATS_QUANTITY" -> {
                editMessage = editSeatsMessage(incomeMessage, trimId(callback));
            }
            case "EDIT_COMMENTARY" -> {
                editMessage = editCommentaryMessage(incomeMessage, trimId(callback));
            }
            case "CHOOSE_REQUEST_TO_CANCEL_CALLBACK" -> {
                editMessage = chooseRequestToCancelMessage(incomeMessage);
            }
            case "CANCEL_REQUEST" -> {
                FindPassengerRequest request = cancelRequest(trimId(callback));
                editMessage = cancelRequestSuccessMessage(incomeMessage, request);
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
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE());
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
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE());
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
                adminHandler.locationButtonsAttributesListCreator(callbackData, settlementId);
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
                adminHandler.locationButtonsAttributesListCreator(callbackData, settlementId);
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
        String todayCallback = FindPassengerOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TODAY;
        String tomorrowCallback = FindPassengerOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TOMORROW;
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: createNewRequestChooseDateMessage");
        return editMessage;
    }

    private void createNewRequestSetDate(long chatId, String day) {
        log.debug("method createNewRequestSetDate");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId);
        LocalDate rideDate = LocalDate.now();
        if (isToday(day)) {
            dto.setDepartureDate(rideDate);

        } else {
            dto.setDepartureDate(rideDate.plusDays(1));
        }
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText createNewRequestTimeMessage(Message incomeMessage) {
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        String chatStatus = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, chatStatus);
        log.debug("method: createNewRequestTimeMessage");
        return editMessage;
    }

    private void createNewRequestSetTime(long chatId, LocalTime time) {
        log.debug("method createNewRequestSetTime");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDepartureTime(time);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private SendMessage createNewRequestChooseCarMessage(long chatId) {
//        TODO Если у пользователя один автомобиль сделать кнопку добавления автомобиля и переделать для соответствия текст
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_CHOSE_CAR_MESSAGE() + carHandler.CarListToString(chatId));
        String callback = FindPassengerOperation.CREATE_REQUEST_CAR_CALLBACK.getValue();
        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(createCarChoiceButtonsAttributesList(callback, chatId)));
        log.debug("method: createNewRequestChooseCarMessage");
        return sendMessage;
    }

    private void createNewRequestSetCar(long chatId, int carId) {
        log.debug("method createNewRequestSetCar");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setCar(carService.findById(carId));
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText createNewRequestSeatsMessage(Message incomeMessage) {
        String chatStatus = FindPassengerOperation.CREATE_REQUEST_SEATS_STATUS.getValue();
        editMessage = createSeatsMessage(incomeMessage, chatStatus);
        log.debug("method: createNewRequestSeatsMessage");
        return editMessage;
    }

    private void createNewRequestSetSeatsQuantity(long chatId, int seatsQuantity) {
        log.debug("method createNewRequestSetSeatsQuantity");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setSeatsQuantity(seatsQuantity);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private SendMessage createNewRequestCommentaryMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.skipButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_SKIP_COMMENT_CALLBACK)); // Skip step button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_COMMENTARY_STATUS);
        log.debug("method: createNewRequestCommentaryMessage");
        return sendMessage;
    }

    private void createNewRequestSetCommentary(long chatId, String commentary) {
        log.debug("method createNewRequestSetCommentary");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setCommentary(firstLetterToUpperCase(commentary));
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText checkDataBeforeSaveMessageSkipComment(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(dtoToString(findPassengerStorageAccess.getDTO(incomeMessage.getChatId())));
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.saveButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.SAVE_REQUEST_CALLBACK)); // Save button
        buttonsAttributesList.add(buttons.editButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_REQUEST_CALLBACK)); // Edit button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method checkDataBeforeSaveMessageSkipComment");
        return editMessage;
    }

    private SendMessage checkDataBeforeSaveMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(dtoToString(findPassengerStorageAccess.getDTO(chatId)));
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.saveButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.SAVE_REQUEST_CALLBACK)); // Save button
        buttonsAttributesList.add(buttons.editButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_REQUEST_CALLBACK)); // Edit button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method checkDataBeforeSaveMessage");
        return sendMessage;
    }

    private EditMessageText saveRequestSuccessMessage(Message incomeMessage, FindPassengerRequest request) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SAVE_SUCCESS_MESSAGE1() + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        log.debug("method saveRequestSuccessMessage");
        return editMessage;
    }

    private EditMessageText startEditBeforeSaveRequestMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.settlementLocationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_SETTLEMENT_LOCATION_CALLBACK.getValue())); // Edit settlements or locations button
        buttonsAttributesList.add(buttons.dateTimeButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_DATE_TIME_CALLBACK.getValue())); // Edit date or time button
        buttonsAttributesList.add(buttons.carDetailsButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CAR_DETAILS_CALLBACK.getValue())); // Change car or seats quantity button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_COMMENTARY_CALLBACK)); // Tomorrow button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: startEditBeforeSaveRequestMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveSettlementLocationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.swapDepartureDestinationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_SWAP_DEPARTURE_DESTINATION_CALLBACK.getValue())); // Swap departure and destination button
        buttonsAttributesList.add(buttons.departureSettlementButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_DEPARTURE_SETTLEMENT_CALLBACK.getValue())); // Edit departure settlement button
        buttonsAttributesList.add(buttons.departureLocationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_DEPARTURE_LOCATION_CALLBACK.getValue())); // Edit destination settlement button
        buttonsAttributesList.add(buttons.destinationSettlementButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_DESTINATION_SETTLEMENT_CALLBACK.getValue())); // Edit departure location button
        buttonsAttributesList.add(buttons.destinationLocationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_DESTINATION_LOCATION_CALLBACK.getValue())); // Edit destination location button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveSettlementLocationMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveDepartureSettlementMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DEPARTURE_SETTLEMENT_CALLBACK.getValue();

        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callbackData, settlementService.findAll());
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDepartureSettlementMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveDepartureLocationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DEPARTURE_LOCATION_MESSAGE());
        int settlementId = findPassengerStorageAccess.getDTO(incomeMessage.getChatId()).getDepartureSettlement().getId();
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DEPARTURE_LOCATION_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.locationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDepartureLocationMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveDestinationSettlementMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);

        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DESTINATION_SETTLEMENT_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DESTINATION_SETTLEMENT_CALLBACK.getValue();

        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callbackData, settlementService.findAll());
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDestinationSettlementMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveDestinationLocationMessage(Message incomeMessage) {

        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DESTINATION_LOCATION_MESSAGE());
        int settlementId = findPassengerStorageAccess.getDTO(incomeMessage.getChatId()).getDestinationSettlement().getId();
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DESTINATION_LOCATION_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.locationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDestinationLocationMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveDateTimeMessage(Message incomeMessage) {
        String callbackDate = FindPassengerOperation.EDIT_BEFORE_SAVE_DATE_CALLBACK.getValue();
        String callbackTime = FindPassengerOperation.EDIT_BEFORE_SAVE_TIME_CALLBACK.getValue();
        editMessage = createDateTimeMessage(incomeMessage, callbackDate, callbackTime);
        log.debug("method: EditBeforeSaveDateTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveCarDetailsMessage(Message incomeMessage) {
        String carCallback = FindPassengerOperation.EDIT_BEFORE_SAVE_CAR_CALLBACK.getValue();
        String seatsCallback = FindPassengerOperation.EDIT_BEFORE_SAVE_SEATS_QUANTITY_CALLBACK.getValue();
        editMessage = createCarDetailsMessage(incomeMessage, carCallback, seatsCallback);
        log.debug("method: EditBeforeSaveCarDetailsMessage");
        return editMessage;
    }

    private void editBeforeSaveSwapDepartureDestination(long chatId) {
        log.debug("method: editBeforeSaveSwapDepartureDestination");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId);
        if (dto.getDirection().equals(Direction.FROM_MINSK.getValue())) {
            dto.setDirection(Direction.TOWARDS_MINSK.getValue());
        } else {
            dto.setDirection(Direction.FROM_MINSK.getValue());
        }
        Settlement settlement = dto.getDepartureSettlement();
        Location location = dto.getDepartureLocation();
        dto.setDepartureSettlement(dto.getDestinationSettlement());
        dto.setDepartureLocation(dto.getDestinationLocation());
        dto.setDestinationSettlement(settlement);
        dto.setDestinationLocation(location);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText editBeforeSaveChangeDateMessage(Message incomeMessage) {
        String todayCallback = FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TODAY;
        String tomorrowCallback = FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TOMORROW;
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: editBeforeSaveChangeDateMessage");
        return editMessage;
    }

    private void editBeforeSaveTimeSendMessage(long chatId) {
        String callback = FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_STATUS.getValue();
        createTimeSendMessage(chatId, callback);
        log.debug("method: editBeforeSaveTimeMessage");
    }

    private EditMessageText editBeforeSaveTimeMessage(Message incomeMessage) {
        String chatStatus = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveChooseCarMessage(Message incomeMessage) {
        String callback = FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_CAR_CALLBACK.getValue();
        editMessage = createChooseCarMessage(incomeMessage, callback);
        log.debug("method: editBeforeSaveChooseCarMessage");
        return editMessage;
    }


    private EditMessageText editBeforeSaveSeatsMessage(Message incomeMessage) {
        String chatStatus = FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_SEATS_QUANTITY_STATUS.getValue();
        editMessage = createSeatsMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveSeatsMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveCommentaryMessage(Message incomeMessage) {
        String chatStatus = FindPassengerOperation.CREATE_REQUEST_COMMENTARY_STATUS.getValue();
        createCommentaryMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveCommentaryMessage");
        return editMessage;
    }

    private EditMessageText chooseRequestToEditMessage(Message incomeMessage) {
        String message = messages.getCHOOSE_REQUEST_TO_EDIT_MESSAGE();
        String callback = FindPassengerOperation.EDIT_REQUEST_START_CALLBACK.getValue();
        editMessage = createChoiceRequestMessage(incomeMessage, message, callback);
        log.debug("method: chooseRequestToEditMessage");
        return editMessage;
    }

    private EditMessageText startEditRequestMessage(Message incomeMessage, int requestId) {
        editMessageTextGeneralPreset(incomeMessage);

        editMessage.setText(requestToString(getLastRequestOptional(incomeMessage.getChatId())) + messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.settlementLocationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_SETTLEMENT_LOCATION_CALLBACK.getValue() + requestId)); // Edit settlements or locations button
        buttonsAttributesList.add(buttons.dateTimeButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_DATE_TIME_CALLBACK.getValue() + requestId)); // Edit date or time button
        buttonsAttributesList.add(buttons.carDetailsButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_CAR_DETAILS_CALLBACK.getValue() + requestId)); // Change car or seats quantity button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_COMMENTARY_CALLBACK.getValue() + requestId)); // Edit commentary button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: startEditRequestMessage");
        return editMessage;
    }

    private EditMessageText editSettlementLocationMessage(Message incomeMessage, int requestId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = createEditRequestButtonsAttributesList(FindPassengerOperation.EDIT_SWAP_DEPARTURE_DESTINATION_CALLBACK.getValue(),
                FindPassengerOperation.EDIT_DEPARTURE_SETTLEMENT_CALLBACK.getValue(),
                FindPassengerOperation.EDIT_DEPARTURE_LOCATION_CALLBACK.getValue(),
                FindPassengerOperation.EDIT_DESTINATION_SETTLEMENT_CALLBACK.getValue(),
                FindPassengerOperation.EDIT_DEPARTURE_LOCATION_CALLBACK.getValue(),
                requestId);

        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveSettlementLocationMessage");

        return editMessage;
    }

    private EditMessageText editSettlementMessage(Message incomeMessage, String messageText, String callbackData, int requestId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callbackData, settlementService.findAll(), requestId);
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: editDepartureSettlementMessage");

        return editMessage;
    }

    //                TODO продумать изменения направления
    private EditMessageText editDepartureSettlementMessage(Message incomeMessage, int requestId) {
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_CHANGE_DEPARTURE_SETTLEMENT_CALLBACK.getValue();
        return editSettlementMessage(incomeMessage, messageText, callbackData, requestId);
    }

    private EditMessageText editDestinationSettlementMessage(Message incomeMessage, int requestId) {
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_CHANGE_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        return editSettlementMessage(incomeMessage, messageText, callbackData, requestId);
    }

    private FindPassengerRequest setEditedDepartureSettlement(int requestId, int settlementId) {
        log.debug("method: setEditedDepartureSettlement");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setDepartureSettlement(settlementService.findById(settlementId));
        return findPassengerRequestService.updateRequest(request);
    }

    private FindPassengerRequest setEditedDestinationSettlement(int requestId, int settlementId) {
        log.debug("method: setEditedDestinationSettlement");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setDestinationSettlement(settlementService.findById(settlementId));
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editLocationMessage(Message incomeMessage, String messageText, String callbackData, int requestId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText);
        int settlementId = findPassengerRequestService.findById(requestId).getDestinationSettlement().getId();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.locationButtonsAttributesListCreator(callbackData, requestId, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: editLocationMessage");

        return editMessage;
    }

    private EditMessageText editDepartureLocationMessage(Message incomeMessage, int requestId) {
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_DEPARTURE_LOCATION_MESSAGE();
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_CHANGE_DEPARTURE_LOCATION_CALLBACK.getValue();
        return editLocationMessage(incomeMessage, messageText, callbackData, requestId);
    }

    private EditMessageText editDestinationLocationMessage(Message incomeMessage, int requestId) {
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_DESTINATION_LOCATION_MESSAGE();
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_CHANGE_DESTINATION_LOCATION_CALLBACK.getValue();
        return editLocationMessage(incomeMessage, messageText, callbackData, requestId);
    }

    private FindPassengerRequest setEditedDepartureLocation(int requestId, int locationId) {
        log.debug("method: setEditedDepartureLocation");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setDepartureLocation(locationService.findById(locationId));
        return findPassengerRequestService.updateRequest(request);
    }

    private FindPassengerRequest setEditedDestinationLocation(int requestId, int locationId) {
        log.debug("method: setEditedDestinationLocation");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setDestinationLocation(locationService.findById(locationId));
        return findPassengerRequestService.updateRequest(request);
    }

    private FindPassengerRequest editSwapDepartureDestination(int requestId) {
        log.debug("method: editSwapDepartureDestination");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        if (request.getDirection().equals(Direction.FROM_MINSK.getValue())) {
            request.setDirection(Direction.TOWARDS_MINSK.getValue());
        } else {
            request.setDirection(Direction.FROM_MINSK.getValue());
        }
        Settlement settlement = request.getDepartureSettlement();
        Location location = request.getDepartureLocation();
        request.setDepartureSettlement(request.getDestinationSettlement());
        request.setDepartureLocation(request.getDestinationLocation());
        request.setDestinationSettlement(settlement);
        request.setDestinationLocation(location);
        findPassengerRequestService.updateRequest(request);
        return request;
    }

    private EditMessageText editDateTimeMessage(Message incomeMessage, int requestId) {
        String callbackDate = FindPassengerOperation.EDIT_DATE_CALLBACK.getValue() + requestId;
        String callbackTime = FindPassengerOperation.EDIT_TIME_CALLBACK.getValue() + requestId;
        editMessage = createDateTimeMessage(incomeMessage, callbackDate, callbackTime);
        log.debug("method: editDateTimeMessage");
        return editMessage;
    }

    private EditMessageText editDateMessage(Message incomeMessage, int requestId) {
        String todayCallback = String.format(FindPassengerOperation.EDIT_CHANGE_DATE_CALLBACK.getValue(), Day.TODAY.getValue(), requestId);
        String tomorrowCallback = String.format(FindPassengerOperation.EDIT_CHANGE_DATE_CALLBACK.getValue(), Day.TOMORROW.getValue(), requestId);
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: editBeforeSaveChangeDateMessage");
        return editMessage;
    }

    private FindPassengerRequest editSetDate(int requestId, String day) {
        log.debug("method editSetDate");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        LocalDateTime rideDate = request.getDepartureAt();
        LocalDate today = LocalDate.now();
        if (isToday(day)) {
            request.setDepartureAt(rideDate.withDayOfMonth(today.getDayOfMonth()));
        } else {
            request.setDepartureAt(rideDate.withDayOfMonth(today.getDayOfMonth() + 1));
        }
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editTimeMessage(Message incomeMessage, int requestId) {
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        String chatStatus = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_CHANGE_TIME_STATUS.getValue() + requestId;
        editMessage = createTimeMessage(incomeMessage, chatStatus);
        log.debug("method: editTimeMessage");
        return editMessage;
    }

    private void editTimeSendValidTimeMessage(long chatId) {
        String callback = FindPassengerOperation.EDIT_CHANGE_TIME_STATUS.getValue();
        createTimeSendMessage(chatId, callback);
        log.debug("method: editTimeSendValidTimeMessage");
    }

    private FindPassengerRequest editSetTime(int requestId, LocalTime time) {
        log.debug("method editSetTime");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        LocalDateTime rideDate = request.getDepartureAt();
        rideDate = rideDate.withHour(time.getHour()).withMinute(time.getMinute());
        request.setDepartureAt(rideDate);
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editCarDetailsMessage(Message incomeMessage, int requestId) {
        String carCallback = FindPassengerOperation.EDIT_CAR_CALLBACK.getValue() + requestId;
        String seatsCallback = FindPassengerOperation.EDIT_SEATS_QUANTITY_CALLBACK.getValue() + requestId;
        editMessage = createCarDetailsMessage(incomeMessage, carCallback, seatsCallback);
        log.debug("method: editCarDetailsMessage");
        return editMessage;
    }

    private EditMessageText editChooseCarMessage(Message incomeMessage, int requestId) {
        String callback = String.format(FindPassengerOperation.EDIT_CHANGE_CAR_CALLBACK.getValue(), requestId);
        editMessage = createChooseCarMessage(incomeMessage, callback);
        log.debug("method: editChooseCarMessage");
        return editMessage;
    }

    private FindPassengerRequest setEditedCar(int requestId, int carId) {
        log.debug("method: setEditedCar");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setCar(carService.findById(carId));
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editSeatsMessage(Message incomeMessage, int requestId) {
        String chatStatus = FindPassengerOperation.EDIT_CHANGE_SEATS_QUANTITY_STATUS.getValue() + requestId;
        editMessage = createSeatsMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveSeatsMessage");
        return editMessage;
    }

    private FindPassengerRequest setEditedSeatsQuantity(int requestId, int quantity) {
        log.debug("method: setEditedSeatsQuantity");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setSeatsQuantity(quantity);
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editCommentaryMessage(Message incomeMessage, int requestId) {
        String chatStatus = FindPassengerOperation.EDIT_CHANGE_COMMENTARY_STATUS.getValue() + requestId;
        createCommentaryMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveCommentaryMessage");
        return editMessage;
    }

    private FindPassengerRequest setEditedCommentary(int requestId, String commentary) {
        log.debug("method: setEditedSeatsQuantity");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setCommentary(commentary);
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editRequestSuccessEditMessage(Message incomeMessage, FindPassengerRequest request) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_SUCCESS_EDITION_MESSAGE() + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.deleteChatStatus(incomeMessage.getChatId());
        return editMessage;
    }

    private SendMessage editRequestSuccessSendMessage(long chatId, FindPassengerRequest request) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getFIND_PASSENGER_SUCCESS_EDITION_MESSAGE() + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.deleteChatStatus(chatId);
        return sendMessage;
    }

    private EditMessageText chooseRequestToCancelMessage(Message incomeMessage) {
        String message = messages.getCHOOSE_REQUEST_TO_CANCEL_MESSAGE();
        String callback = FindPassengerOperation.CANCEL_REQUEST_CALLBACK.getValue();
        editMessage = createChoiceRequestMessage(incomeMessage, message, callback);
        log.debug("method: chooseRequestToEditMessage");
        return editMessage;
    }

    private EditMessageText cancelRequestSuccessMessage(Message incomeMessage, FindPassengerRequest request) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_CANCEL_REQUEST_SUCCESS_MESSAGE() + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }

    private FindPassengerRequest saveRequest(long chatId) {
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId);
        findPassengerStorageAccess.delete(chatId);
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("method saveRequest");
        return findPassengerRequestService.addNewRequest(dto);
    }

    private FindPassengerRequest cancelRequest(int requestId) {
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setActive(false)
                .setCanceled(true)
                .setCanceledAt(LocalDateTime.now());
        log.debug("method cancelRequest");
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText createDateTimeMessage(Message incomeMessage, String callbackDate, String callbackTime) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.dateButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + callbackDate)); // Edit date button
        buttonsAttributesList.add(buttons.timeButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + callbackTime)); // Edit time button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createDateTimeMessage");
        return editMessage;
    }

    private void createTimeSendMessage(long chatId, String chatStatus) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));

        chatStatusStorageAccess.addChatStatus(chatId, Handlers.FIND_PASSENGER.getHandlerPrefix() + chatStatus);
        log.debug("method: createTimeSendMessage");
        sendBotMessage(sendMessage);
    }

    private EditMessageText createTimeMessage(Message incomeMessage, String chatStatus) {
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), chatStatus);
        log.debug("method: createTimeMessage");
        return editMessage;
    }

    private EditMessageText createChooseCarMessage(Message incomeMessage, String callback) {
//        TODO Если у пользователя один автомобиль сделать кнопку добавления автомобиля и переделать для соответствия текст
        editMessageTextGeneralPreset(incomeMessage);
        Long chatId = incomeMessage.getChatId();
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_CHOSE_CAR_MESSAGE() + carHandler.CarListToString(chatId));
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(createCarChoiceButtonsAttributesList(callback, chatId)));
        log.debug("method: createChooseCarMessage");
        return editMessage;
    }

    private EditMessageText createCarDetailsMessage(Message incomeMessage, String carCallback, String seatsCallback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.carButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + carCallback)); // Change car button
        buttonsAttributesList.add(buttons.seatsQuantityButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + seatsCallback)); // Edit seats quantity button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createCarDetailsMessage");
        return editMessage;
    }

    private EditMessageText createSeatsMessage(Message incomeMessage, String chatStatus) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SEATS_MESSAGE());
        editMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + chatStatus);
        log.debug("method: createSeatsMessage");
        return editMessage;
    }

    private EditMessageText createCommentaryMessage(Message incomeMessage, String chatStatus) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + chatStatus);
        log.debug("method: createCommentaryMessage");
        return editMessage;
    }

    private EditMessageText createChoiceRequestMessage(Message incomeMessage, String message, String callback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(message + requestListToString(incomeMessage.getChatId()));
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + callback;
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(requestButtonsAttributesListCreator(callbackData, incomeMessage.getChatId())));
        log.debug("method: createChoiceRequestMessage");
        return editMessage;
    }

    private SendMessage handleReceivedIncorrectTime(LocalTime time, long chatId) {
        if (time.toNanoOfDay() == 100) {
            sendMessage = createNewRequestInvalidTimeFormatMessage(chatId);
        } else {
            sendMessage = createNewRequestExpiredTimeMessage(chatId);
        }
        return sendMessage;
    }


    public FindPassengerRequest getLastRequest(long chatId) {
        return findPassengerRequestService.findLastUserRequest(chatId);
    }

    public Optional<FindPassengerRequest> getLastRequestOptional(long chatId) {
        return findPassengerRequestService.findLastUserRequestOptional(chatId);
    }

    private List<FindPassengerRequest> getUserActiveFindPassengerRequestsList(long chatId) {
        return findPassengerRequestService.usersActiveRequestList(chatId);
    }

    private EditMessageText createChooseDateMessage(Message incomeMessage, String todayCallback, String tomorrowCallback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DATE_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.todayButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + todayCallback)); // Today button
        buttonsAttributesList.add(buttons.tomorrowButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + tomorrowCallback)); // Tomorrow button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createChooseDateMessage");
        return editMessage;
    }

    private List<Pair<String, String>> createEditRequestButtonsAttributesList(String swapCallback, String depSettlementCallback, String depLocationCallback, String destSettlementCallback, String destLocationCallback, int requestId) {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.swapDepartureDestinationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + swapCallback + requestId)); // Swap departure and destination button
        buttonsAttributesList.add(buttons.departureSettlementButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + depSettlementCallback + requestId)); // Edit departure settlement button
        buttonsAttributesList.add(buttons.departureLocationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + depLocationCallback + requestId)); // Edit destination settlement button
        buttonsAttributesList.add(buttons.destinationSettlementButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + destSettlementCallback + requestId)); // Edit departure location button
        buttonsAttributesList.add(buttons.destinationLocationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + destLocationCallback + requestId)); // Edit destination location button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private List<Pair<String, String>> createCarChoiceButtonsAttributesList(String callback, long chatId) {
        List<Pair<String, String>> buttonsAttributesList =
                carHandler.carButtonsAttributesListCreator(Handlers.FIND_PASSENGER.getHandlerPrefix() + callback, chatId);
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
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

    private void expiredTimeMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_EXPIRED_TIME_MESSAGE2());
        sendMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        sendBotMessage(sendMessage);
        log.debug("method: expiredTimeMessage");
    }

    private SendMessage invalidSeatsQuantityFormatMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SEATS_QUANTITY_INVALID_FORMAT_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        log.debug("method: createNewRequestInvalidTimeFormatMessage");
        return sendMessage;
    }

    private EditMessageText noActiveRequestsMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_NO_ACTIVE_REQUEST_MESSAGE());
        log.info("noActiveRequestsMessage");
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return editMessage;
    }

    public List<Pair<String, String>> requestButtonsAttributesListCreator(String callbackData, long chatId) {
        List<FindPassengerRequest> requestList = getUserActiveFindPassengerRequestsList(chatId);
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        if (requestList.isEmpty()) {
            buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        } else {
            Map<Integer, String> requestButtonsAttributes = requestList
                    .stream()
                    .collect(Collectors.toMap(request -> request.getId(), request -> String.valueOf(requestList.indexOf(request) + 1)));
            buttonsAttributesList = buttons.buttonsAttributesListCreator(requestButtonsAttributes, callbackData);
            buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        }
        return buttonsAttributesList;
    }

    public String requestListToString(long chatId) {
        List<FindPassengerRequest> requests = getUserActiveFindPassengerRequestsList(chatId);
        if (requests.isEmpty()) {
            return messages.getFIND_PASSENGER_NO_ACTIVE_REQUEST_MESSAGE();
        } else {
            StringBuilder text = new StringBuilder();
            for (FindPassengerRequest request : requests) {
                int n = requests.indexOf(request) + 1;
                text.append(n).append(". ").append(requestToString(request)).append("\n");
            }
            return text.toString();
        }
    }

    public String requestToString(FindPassengerRequest request) {
        String messageText = String.format(messages.getFIND_PASSENGER_REQUEST_TO_STRING_MESSAGE(),
                request.getUser().getFirstName(),
                request.getDepartureSettlement().getName(),
                request.getDepartureLocation().getName(),
                request.getDestinationSettlement().getName(),
                request.getDestinationLocation().getName(),
                request.getDepartureAt().toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                request.getDepartureAt().toLocalTime().toString(),
                request.getCar().getModel(),
                request.getCar().getPlateNumber(),
                request.getSeatsQuantity(),
                request.getCommentary(),
                request.getCreatedAt().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
        return messageText;
    }

    public String requestToString(Optional<FindPassengerRequest> optional) {
        String messageText;
        if (optional.isPresent()) {
            messageText = requestToString(optional.get());
        } else messageText = messages.getFIND_PASSENGER_NO_ACTIVE_REQUEST_MESSAGE();
        return messageText;
    }

    private String dtoToString(FindPassengerRequestDTO dto) {
        String messageText = String.format(messages.getCREATE_FIND_PASSENGER_REQUEST_CHECK_DATA_BEFORE_SAVE_MESSAGE(),
                dto.getUser().getFirstName(),
                dto.getDepartureSettlement().getName(),
                dto.getDepartureLocation().getName(),
                dto.getDestinationSettlement().getName(),
                dto.getDestinationLocation().getName(),
                dto.getDepartureDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                dto.getDepartureTime().toString(),
                dto.getCar().getModel(),
                dto.getCar().getPlateNumber(),
                dto.getSeatsQuantity(),
                dto.getCommentary());
        return messageText;
    }

    private boolean isToday(String day) {
//        TODO добавить проверку day на совпадение со значениями enum Day
        boolean isToday = day.equals(String.valueOf(Day.TODAY));
        log.debug("method isToday = " + isToday);
        return isToday;
    }

    private boolean isExpired(long chatId, LocalTime time) {
        return findPassengerStorageAccess.getDTO(chatId).getDepartureDate().isEqual(LocalDate.now()) && time.isBefore(LocalTime.now());
    }

    private boolean isExpired(long chatId) {
        return findPassengerStorageAccess.getDTO(chatId).getDepartureTime().isBefore(LocalTime.now());
    }

    private boolean isExpired(int requestId) {
        return findPassengerRequestService.findById(requestId).getDepartureAt().isBefore(LocalDateTime.now());
    }

    private boolean isExpired(int requestId, LocalTime time) {
        return findPassengerRequestService.findById(requestId).getDepartureAt().toLocalDate().isEqual(LocalDate.now()) && time.isBefore(LocalTime.now());
    }

    private LocalTime getTime(String timeString) {
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

    private boolean seatsQuantityIsValid(String s) {
        return Character.isDigit(s.charAt(0)) && s.length() == 1 && (Integer.parseInt(s) > 0 & Integer.parseInt(s) < 5);
    }
    private boolean isRequestQuantityLimit (long chatId){
        return getUserActiveFindPassengerRequestsList(chatId).size()>3;
    }

    private void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }
    private SendMessage nextStep(long chatId) {
//        TODO удалить метод по окончании реализации всего функционала
        sendMessage.setChatId(chatId);
        sendMessage.setText("nextStep");
        sendMessage.setReplyMarkup(null);
        log.debug("method: nextStep");

        return sendMessage;
    }

    private EditMessageText nextStep(Message incomemessage) {
        //        TODO удалить метод по окончании реализации всего функционала
        editMessageTextGeneralPreset(incomemessage);
        editMessage.setText("nextStep");
        editMessage.setReplyMarkup(null);
        log.debug("method: nextStep");

        return editMessage;
    }
}
