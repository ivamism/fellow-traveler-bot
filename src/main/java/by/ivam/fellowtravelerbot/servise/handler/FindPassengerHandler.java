package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.bot.enums.Day;
import by.ivam.fellowtravelerbot.bot.enums.FindPassengerOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.model.Direction;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
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
    @Autowired
    CarHandler carHandler;

    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    // TODO добавить перед началом проверку на наличие  у юзера автомобиля
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
                if (time.toNanoOfDay() == 100) {
                    sendMessage = createNewRequestInvalidTimeFormatMessage(chatId);
                } else if (isExpired(chatId, time)) {
                    sendMessage = createNewRequestExpiredTimeMessage(chatId);
                } else {
                    createNewRequestSetTime(chatId, time);
                    sendMessage = createNewRequestChooseCarMessage(incomeMessage);
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
                if (messageText.length() <= 1000) sendMessage = nextStep(chatId);
                sendMessage = checkDataBeforeSaveMessage(incomeMessage);
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
                if (isToday(day)) editMessage = createNewRequestTimeTodayMessage(incomeMessage);
                else editMessage = createNewRequestTimeTomorrowMessage(incomeMessage);
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
            case "EDIT_BEFORE_SAVE_DATE_TAME" -> {
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
            case "EDIT_BEFORE_SAVE_DATE_CALLBACK" -> {
                editMessage = editBeforeSaveChangeDateMessage(incomeMessage);
            }

            case "EDIT_BEFORE_SAVE_CHANGE_DATE" -> {
                String day = trimSecondSubstring(callback);
                createNewRequestSetDate(chatId, day);
                if (isToday(day) && isExpired(chatId)) {
                    expiredTimeMessage(incomeMessage);
                    editMessage = editBeforeSaveTimeMessage(incomeMessage);
                } else editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
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

    private EditMessageText createNewRequestTimeTodayMessage(Message incomeMessage) {
//        TODO добавить  кнопки с промежутками времени.
//        переделать в один метод, который в зависимости от даты выводит разную клавиатуру

        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_TIME_STATUS);
        log.debug("method: createNewRequestChooseTimeTodayMessage");

        return editMessage;
    }

    private EditMessageText createNewRequestTimeTomorrowMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_TIME_STATUS);
        log.debug("method: createNewRequestChooseTimeTomorrowMessage");

        return editMessage;
    }

    private void createNewRequestSetTime(long chatId, LocalTime time) {
        log.debug("method createNewRequestSetTime");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDepartureTime(time);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private SendMessage createNewRequestChooseCarMessage(Message incomeMessage) {
//        TODO Если у пользователя один автомобиль сделать кнопку добавления автомобиля и переделать для соответствия текст
        Long chatId = incomeMessage.getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_CHOSE_CAR_MESSAGE() + carHandler.CarListToSring(chatId));

        List<Pair<String, String>> buttonsAttributesList =
                carHandler.CarButtonsAttributesListCreator(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_CAR_CALLBACK.getValue(), chatId);
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        log.debug("method: createNewRequestChooseCarMessage");
        return sendMessage;
    }

    private void createNewRequestSetCar(long chatId, int carId) {
        log.debug("method createNewRequestSetCar");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setCar(carService.findById(carId));
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText createNewRequestSeatsMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SEATS_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_SEATS_STATUS);
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
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(incomeMessage.getChatId());
        String username = dto.getUser().getFirstName();
        String departureSettlement = dto.getDepartureSettlement().getName();
        String departureLocation = dto.getDepartureLocation().getName();
        String destinationSettlement = dto.getDestinationSettlement().getName();
        String destinationLocation = dto.getDestinationLocation().getName();
        String date = dto.getDepartureDate().toString();
        String time = dto.getDepartureTime().toString();
        String car = dto.getCar().getModel();
        String plates = dto.getCar().getPlateNumber();
        int seats = dto.getSeatsQuantity();
        String commentary = dto.getCommentary();
        String messageText = String.format(messages.getCREATE_FIND_PASSENGER_REQUEST_CHECK_DATA_BEFORE_SAVE_MESSAGE(),
                username,
                departureSettlement,
                departureLocation,
                destinationSettlement,
                destinationLocation,
                date,
                time,
                car,
                plates,
                seats,
                commentary);
        editMessage.setText(messageText);
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
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId);
        sendMessage.setChatId(chatId);
        String username = dto.getUser().getFirstName();
        String departureSettlement = dto.getDepartureSettlement().getName();
        String departureLocation = dto.getDepartureLocation().getName();
        String destinationSettlement = dto.getDestinationSettlement().getName();
        String destinationLocation = dto.getDestinationLocation().getName();
        String date = dto.getDepartureDate().toString();
        String time = dto.getDepartureTime().toString();
        String car = dto.getCar().getModel();
        String plates = dto.getCar().getPlateNumber();
        int seats = dto.getSeatsQuantity();
        String commentary = dto.getCommentary();
        String messageText = String.format(messages.getCREATE_FIND_PASSENGER_REQUEST_CHECK_DATA_BEFORE_SAVE_MESSAGE(),
                username,
                departureSettlement,
                departureLocation,
                destinationSettlement,
                destinationLocation,
                date,
                time,
                car,
                plates,
                seats,
                commentary);
        sendMessage.setText(messageText);
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
        String username = request.getUser().getFirstName();
        String departureSettlement = request.getDepartureSettlement().getName();
        String departureLocation = request.getDepartureLocation().getName();
        String destinationSettlement = request.getDestinationSettlement().getName();
        String destinationLocation = request.getDestinationLocation().getName();
        String date = request.getDepartureDate().toString();
        String time = request.getDepartureTime().toString();
        String car = request.getCar().getModel();
        String plates = request.getCar().getPlateNumber();
        int seats = request.getSeatsQuantity();
        String commentary = request.getCommentary();
        String messageText = String.format(messages.getCREATE_FIND_PASSENGER_REQUEST_SAVE_SUCCESS_MESSAGE(),
                username,
                departureSettlement,
                departureLocation,
                destinationSettlement,
                destinationLocation,
                date,
                time,
                car,
                plates,
                seats,
                commentary);
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        log.debug("method saveRequestSuccessMessage");
        return editMessage;
    }

    private EditMessageText startEditBeforeSaveRequestMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_EDIT_BEFORE_SAVE_START_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.settlementLocationButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_SETTLEMENT_LOCATION_CALLBACK.getValue())); // Edit settlements or locations button
        buttonsAttributesList.add(buttons.dateTimeButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_DATE_TAME_CALLBACK.getValue())); // Edit date or time button
        buttonsAttributesList.add(buttons.carDetailsButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CAR_DETAILS_CALLBACK.getValue())); // Change car or seats quantity button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_COMMENTARY_CALLBACK)); // Tomorrow button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: startEditBeforeSaveRequestMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveSettlementLocationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_EDIT_BEFORE_SAVE_START_MESSAGE());
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
                adminHandler.departureLocationButtonsAttributesListCreator(callbackData, settlementId);
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
                adminHandler.departureLocationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDestinationLocationMessage");

        return editMessage;
    }


    private EditMessageText editBeforeSaveDateTimeMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_EDIT_BEFORE_SAVE_START_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)

        buttonsAttributesList.add(buttons.dateButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_DATE_CALLBACK)); // Edit date button
        buttonsAttributesList.add(buttons.timeButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_TIME_CALLBACK)); // Edit time button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDateTimeMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveCarDetailsMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_EDIT_BEFORE_SAVE_START_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.carButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CAR_CALLBACK)); // Change car button
        buttonsAttributesList.add(buttons.seatsQuantityButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_SEATS_QUANTITY_CALLBACK)); // Edit seats quantity button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveCarDetailsMessage");

        return editMessage;
    }

    private void editBeforeSaveSwapDepartureDestination(long chatId) {
        log.debug("method: editBeforeSaveSwapDepartureDestination");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId);
        Settlement settlement = dto.getDepartureSettlement();
        Location location = dto.getDepartureLocation();
        dto.setDepartureSettlement(dto.getDestinationSettlement());
        dto.setDepartureLocation(dto.getDestinationLocation());
        dto.setDestinationSettlement(settlement);
        dto.setDestinationLocation(location);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private EditMessageText editBeforeSaveChangeDateMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_DATE_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.todayButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TODAY)); // Today button
        buttonsAttributesList.add(buttons.tomorrowButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TOMORROW)); // Tomorrow button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: editBeforeSaveChangeDateMessage");

        return editMessage;
    }

    private EditMessageText editBeforeSaveTimeMessage(Message incomeMessage) {
//        TODO добавить  кнопки с промежутками времени.
//        переделать в один метод, который в зависимости от даты выводит разную клавиатуру

        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_CALLBACK);
        log.debug("method: editBeforeSaveTimeMessage");

        return editMessage;
    }

    private FindPassengerRequest saveRequest(long chatId) {
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId);
        findPassengerStorageAccess.delete(chatId);
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("method saveRequest");
        return findPassengerRequestService.addNewRequest(dto);
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
        return Character.isDigit(s.charAt(0)) && s.length() == 1 && (Integer.parseInt(s) > 0 & Integer.parseInt(s) < 9);
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

    private void expiredTimeMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_EXPIRED_TIME_MESSAGE2());
        editMessage.setReplyMarkup(null); //set null to remove no longer necessary inline keyboard
        sendEditMessage(editMessage);
        log.debug("method: expiredTimeMessage");
    }

    private SendMessage invalidSeatsQuantityFormatMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_SEATS_QUANTITY_INVALID_FORMAT_MESSAGE());
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

    private EditMessageText nextStep(Message incomemessage) {
        editMessageTextGeneralPreset(incomemessage);
        editMessage.setText("nextStep");
        log.debug("method: nextStep");

        return editMessage;
    }

    private void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }


}
