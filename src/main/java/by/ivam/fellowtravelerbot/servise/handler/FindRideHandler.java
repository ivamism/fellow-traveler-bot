package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.DTO.stateOperations.interfaces.FindRideDtoOperations;
import by.ivam.fellowtravelerbot.bot.enums.Day;
import by.ivam.fellowtravelerbot.bot.enums.Direction;
import by.ivam.fellowtravelerbot.bot.enums.FindPassengerRequestOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.model.Settlement;
import lombok.Data;
import lombok.EqualsAndHashCode;
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


// This class handle operations with search of hitchhiker's rides
@EqualsAndHashCode(callSuper = true)
@Service
@Data
@Log4j
public class FindRideHandler extends RequestHandler implements HandlerInterface {
    @Autowired
    private final FindRideDtoOperations findRideDtoOperations;
    @Autowired
    private final AdminHandler adminHandler;
    private final String handlerPrefix = Handlers.FIND_RIDE.getHandlerPrefix();
    private SendMessage sendMessage = new SendMessage();
    private EditMessageText editMessage = new EditMessageText();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        String messageText = incomeMessage.getText();
        Long chatId = incomeMessage.getChatId();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus + ". message: " + messageText);
        String process = chatStatus;
        if (chatStatus.contains(":")) process = extractProcess(chatStatus);
        switch (process) {
            case "CREATE_REQUEST_TIME_STATUS" -> {
                LocalTime time = getTime(messageText);
                if (time.toNanoOfDay() == 100 || isExpired(chatId, time)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    setDtoTime(chatId, time);
                    sendMessage = createNewRequestSeatsMessage(chatId);
                }
            }
            case "CREATE_REQUEST_SEATS_STATUS" -> {
                if (seatsQuantityIsValid(messageText)) {
                    setDtoPassengersQuantity(chatId, Integer.parseInt(messageText));
                    sendMessage = createNewRequestCommentaryMessage(chatId);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "CREATE_REQUEST_COMMENTARY_STATUS" -> {
                setDtoCommentary(chatId, messageText);
                if (messageText.length() >= 1000) sendMessage = nextStep(chatId);
                else sendMessage = checkDataBeforeSaveMessage(chatId);
//                TODO добавить сообщение если комментарий слишком длинный
            }
            case "EDIT_BEFORE_SAVE_CHANGE_TIME" -> {
                LocalTime time = getTime(messageText);
                if (time.toNanoOfDay() == 100 || isExpired(chatId, time)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    setDtoTime(chatId, time);
                    sendMessage = checkDataBeforeSaveMessage(chatId);
                }
            }
            case "EDIT_BEFORE_SAVE_CHANGE_SEATS_QUANTITY_STATUS" -> {
                if (seatsQuantityIsValid(messageText)) {
                    setDtoPassengersQuantity(chatId, Integer.parseInt(messageText));
                    sendMessage = checkDataBeforeSaveMessage(chatId);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "EDIT_CHANGE_TIME" -> {
                LocalTime time = getTime(messageText);
                int requestId = extractId(chatStatus, getFIRST_VALUE());
                if (time.toNanoOfDay() == 100 || isExpired(requestId, time)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    FindRideRequest request = editSetTime(requestId, time);
                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                }
            }
            case "EDIT_CHANGE_SEATS_QUANTITY" -> {
                if (seatsQuantityIsValid(messageText)) {
                    FindRideRequest request = setEditedSeatsQuantity(extractId(chatStatus, getFIRST_VALUE()), Integer.parseInt(messageText));
                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "EDIT_CHANGE_COMMENTARY" -> {
                FindRideRequest request = setEditedCommentary(extractId(chatStatus, getFIRST_VALUE()), messageText);
                if (messageText.length() >= 1000) sendMessage = nextStep(chatId);
                else sendMessage = editRequestSuccessSendMessage(chatId, request);
//                TODO добавить сообщение если комментарий слишком длинный
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
            process = extractProcess(callback);
        }
        switch (process) {
            case "CREATE_REQUEST" -> {
                if (isRequestQuantityLimit(chatId)) editMessage = sendNecessityToCancelMessage(incomeMessage);
                else {
                    createRequestDTO(chatId);
                    editMessage = createNewRequestChoseDirectionMessage(incomeMessage);
                }
            }
            case "CREATE_REQUEST_DIRECTION" -> {
                String direction = extractParameter(callback, getFIRST_VALUE());
                setDTODirection(chatId, direction);
                int settlementId = settlementService.findByName("Минск").getId();
                if (direction.equals(String.valueOf(Direction.FROM_MINSK))) {
                    setDTODepartureSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseResidenceAsDestinationMessage(incomeMessage);
                } else if ((direction.equals(String.valueOf(Direction.TOWARDS_MINSK)))) {
                    setDtoDestinationSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseResidenceAsDepartureMessage(incomeMessage);
                }
            }
            case "CREATE_REQ_DEP_SETTLEMENT" -> {
                int settlementId = extractId(callback, getFIRST_VALUE());
                if (settlementId == -1) {
                    editMessage = createNewRequestChooseAnotherSettlementAsDepartureMessage(incomeMessage);
                } else {
                    setDTODepartureSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDateMessage(incomeMessage);
                }
            }
            case "CREATE_REQ_DEST_SETTLEMENT" -> {
                int settlementId = extractId(callback, getFIRST_VALUE());
                if (settlementId == -1) {
                    editMessage = createNewRequestChooseAnotherSettlementAsDestinationMessage(incomeMessage);
                } else {
                    setDtoDestinationSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDateMessage(incomeMessage);
                }
            }
            case "CREATE_REQUEST_DATE" -> {
                String day = extractParameter(callback, getFIRST_VALUE());
                setDtoDate(chatId, day);
                editMessage = createNewRequestTimeMessage(incomeMessage);
            }
            case "CREATE_REQUEST_SKIP_COMMENT_CALLBACK" -> {
                setDtoCommentary(chatId, "-");
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "SAVE_REQUEST_CALLBACK" -> {
                FindRideRequest request = saveRequest(chatId);
                editMessage = saveRequestSuccessMessage(incomeMessage, request);
            }
            case "EDIT_REQUEST_BEFORE_SAVE_CALLBACK" -> {
                editMessage = startEditBeforeSaveRequestMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_SETTLEMENT_LOCATION" -> {
                editMessage = editBeforeSaveSettlementLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DATE_TIME" -> {
                editMessage = editBeforeSaveDateTimeMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEP_SETTLEMENT" -> {
                editMessage = editBeforeSaveDepartureSettlementMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEP_SETTLEMENT" -> {
                setDTODepartureSettlement(chatId, extractId(callback, getFIRST_VALUE()));
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEST_SETTLEMENT" -> {
                editMessage = editBeforeSaveDestinationSettlementMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEST_SETTLEMENT" -> {
                setDtoDestinationSettlement(chatId, extractId(callback, getFIRST_VALUE()));
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
                String day = extractParameter(callback, getFIRST_VALUE());
                setEditedDtoDate(chatId, day);
                if (isExpired(chatId)) {
                    expiredTimeMessage(chatId);
                    editBeforeSaveTimeSendMessage(chatId);
                } else editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_TIME" -> {
                editMessage = editBeforeSaveTimeMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_SEATS_QUANTITY" -> {
                editMessage = editBeforeSaveSeatsMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_COMMENTARY_CALLBACK" -> {
                editMessage = editBeforeSaveCommentaryMessage(incomeMessage);
            }
            case "CHOOSE_REQUEST_TO_CANCEL_CALLBACK" -> {
                editMessage = chooseRequestToCancelMessage(incomeMessage);
            }
            case "CANCEL_REQUEST" -> {
                FindRideRequest request = cancelRequest(extractId(callback, getFIRST_VALUE()));
                editMessage = sendCancelRequestSuccessMessage(incomeMessage, request);
            }
            case "CHOOSE_REQUEST_TO_EDIT" -> {
                editMessage = chooseRequestToEditMessage(incomeMessage);
            }
            case "EDIT_REQUEST_START" -> {
                editMessage = startEditRequestMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "NO_ACTIVE_REQUEST" -> {
                editMessage = noActiveRequestsMessage(incomeMessage);
            }
            case "EDIT_SETTLEMENT_LOCATION" -> {
                editMessage = editSettlementLocationMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_SWAP_DEP_DEST" -> {
                FindRideRequest request = editSwapDepartureDestination(extractId(callback, getFIRST_VALUE()));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DEP_SETTLEMENT" -> {
                editMessage = editDepartureSettlementMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_CHANGE_DEP_SETTLEMENT" -> {
                FindRideRequest request = setEditedDepartureSettlement(extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DEST_SETTLEMENT" -> {
                editMessage = editDestinationSettlementMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_CHANGE_DEST_SETTLEMENT" -> {
                FindRideRequest request = setEditedDestinationSettlement(extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DATE_TIME" -> {
                editMessage = editDateTimeMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_DATE" -> {
                editMessage = editDateMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_CHANGE_DATE" -> {
                int requestId = extractId(callback, getSECOND_VALUE());
                FindRideRequest request = editSetDate(requestId, extractParameter(callback, getFIRST_VALUE()));
                if (isExpired(requestId)) {
                    expiredTimeMessage(chatId);
                    editTimeSendValidTimeMessage(chatId, requestId);
                } else {
                    editMessage = editRequestSuccessEditMessage(incomeMessage, request);
                }
            }
            case "EDIT_TIME" -> {
                editMessage = editTimeMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_SEATS_QUANTITY" -> {
                editMessage = editSeatsMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_COMMENTARY" -> {
                editMessage = editCommentaryMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
        }
        sendEditMessage(editMessage);
    }

    public void startCreateNewRequest(long chatId) {
        String messageText = messages.getCREATE_FIND_RIDE_REQUEST_START_PROCESS_MESSAGE();
        sendMessage = createNewRequest(chatId, messageText, handlerPrefix);
        log.debug("method: startCreateNewRequest");
        sendBotMessage(sendMessage);
    }

    private void createRequestDTO(long chatId) {
        FindPassengerRequestDTO dto = new FindPassengerRequestDTO();
        FindRideRequestDTO requestDTO = new FindRideRequestDTO();
        requestDTO.setUser(userService.findUserById(chatId));
        findRideDtoOperations.addFindRideDTO(chatId, requestDTO);
        log.debug("method: createFindPassengerRequestDTO - create DTO " + dto + " and save it in storage");
    }

    private EditMessageText createNewRequestChoseDirectionMessage(Message incomeMessage) {
        editMessage = createChoseDirectionMessage(incomeMessage, handlerPrefix);
        log.debug("method: createNewRequestChoseDirectionMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseResidenceAsDepartureMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_SETTLEMENT_DEPARTURE_CALLBACK.getValue();
        editMessage = createChooseResidenceMessage(incomeMessage, messageText, callback);
        log.debug("method: createNewRequestChooseResidenceAsDepartureMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseAnotherSettlementAsDepartureMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_SETTLEMENT_DEPARTURE_CALLBACK.getValue();
        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        editMessage = createChooseAnotherSettlementMessage(incomeMessage, settlementList, messageText, callback);
        log.debug("method: createNewRequestChooseAnotherSettlementAsDepartureMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseResidenceAsDestinationMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseResidenceMessage(incomeMessage, messageText, callback);
        log.debug("method: createNewRequestChooseResidenceAsDestinationMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseAnotherSettlementAsDestinationMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        //        TODO вынести settlementList в createChooseAnotherSettlementMessage?
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        editMessage = createChooseAnotherSettlementMessage(incomeMessage, settlementList, messageText, callback);
        log.debug("method: createNewRequestChooseAnotherSettlementAsDestinationMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseDateMessage(Message incomeMessage) {
        String todayCallback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TODAY;
        String tomorrowCallback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TOMORROW;
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: createNewRequestChooseDateMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestTimeMessage(Message incomeMessage) {
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        String messageText = messages.getCREATE_FIND_RIDE_REQUEST_TIME_MESSAGE();
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, messageText, chatStatus);
        log.debug("method: createNewRequestTimeMessage");
        return editMessage;
    }

    private void setDTODirection(long chatId, String direction) {
        log.debug("method createNewRequestSetDirection");
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId).setDirection(direction);
        findRideDtoOperations.update(chatId, dto);
    }

    private void setDTODepartureSettlement(long chatId, int settlementId) {
        log.debug("method setDTODepartureSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId).setDepartureSettlement(settlement);
        findRideDtoOperations.update(chatId, dto);
    }

    private void setDtoDestinationSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDestinationSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId).setDestinationSettlement(settlement);
        findRideDtoOperations.update(chatId, dto);
    }

    private void setDtoDate(long chatId, String day) {
        log.debug("method createNewRequestSetDate");
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId);
        if (isToday(day)) {
            dto.setDepartureBefore(LocalDate.now().atTime(0, 0));
        } else {
            dto.setDepartureBefore(LocalDate.now().atTime(0, 0).plusDays(1));
        }
        findRideDtoOperations.update(chatId, dto);
    }

    private void setEditedDtoDate(long chatId, String day) {
        log.debug("method createNewRequestSetDate");
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId);
        if (isToday(day)) {
            dto.setDepartureBefore(dto.getDepartureBefore().withDayOfMonth(LocalDate.now().getDayOfMonth()));
        } else {
            dto.setDepartureBefore(dto.getDepartureBefore().withDayOfMonth(LocalDate.now().getDayOfMonth()).plusDays(1));
        }
        findRideDtoOperations.update(chatId, dto);
    }

    private void setDtoTime(long chatId, LocalTime time) {
        log.debug("method createNewRequestSetTime");
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId);
        dto.setDepartureBefore(dto.getDepartureBefore().withHour(time.getHour()).withMinute(time.getMinute()));
        findRideDtoOperations.update(chatId, dto);
    }

    private SendMessage createNewRequestSeatsMessage(long chatId) {
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_SEATS_STATUS.getValue();
        sendMessage = createSeatsMessage(chatId, chatStatus);
        log.debug("method: createNewRequestSeatsMessage");
        return sendMessage;
    }

    private void setDtoPassengersQuantity(long chatId, int passengersQuantity) {
        log.debug("method setDtoPassengersQuantity");
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId).setPassengersQuantity(passengersQuantity);
        findRideDtoOperations.update(chatId, dto);
    }

    private SendMessage createNewRequestCommentaryMessage(long chatId) {
        sendMessage = createCommentaryMessage(chatId, handlerPrefix);
        log.debug("method: createNewRequestCommentaryMessage");
        return sendMessage;
    }

    private void setDtoCommentary(long chatId, String commentary) {
        log.debug("method setDtoCommentary");
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId).setCommentary(firstLetterToUpperCase(commentary));
        findRideDtoOperations.update(chatId, dto);
    }

    private EditMessageText checkDataBeforeSaveMessageSkipComment(Message incomeMessage) {
        log.debug("method checkDataBeforeSaveMessageSkipComment");
        String messageText = dtoToString(findRideDtoOperations.getDTO(incomeMessage.getChatId()));
        editMessage = createCheckDataBeforeSaveMessageSkipComment(incomeMessage, messageText, handlerPrefix);
        return editMessage;
    }

    private SendMessage checkDataBeforeSaveMessage(long chatId) {
        log.debug("method checkDataBeforeSaveMessage");
        String messageText = dtoToString(findRideDtoOperations.getDTO(chatId));
        sendMessage = createCheckDataBeforeSaveMessage(chatId, messageText, handlerPrefix);
        return sendMessage;
    }

    private EditMessageText saveRequestSuccessMessage(Message incomeMessage, FindRideRequest request) {
        String messageText = requestToString(request);
        editMessage = createRequestSaveSuccessMessage(incomeMessage, messageText);
        log.debug("method saveRequestSuccessMessage");
        return editMessage;
    }

    private EditMessageText startEditBeforeSaveRequestMessage(Message incomeMessage) {
        editMessage = createStartEditRequestMessage(incomeMessage, createEditButtonsAttributesList(handlerPrefix));
        log.debug("method: startEditBeforeSaveRequestMessage");
        return editMessage;
    }

    private List<Pair<String, String>> createEditButtonsAttributesList(String handlerPrefix) {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.settlementLocationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_SETTLEMENT_LOCATION_CALLBACK.getValue())); // Edit settlements or locations button
        buttonsAttributesList.add(buttons.dateTimeButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DATE_TIME_CALLBACK.getValue())); // Edit date or time button
        buttonsAttributesList.add(buttons.passengerQuantityButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_SEATS_QUANTITY_CALLBACK.getValue())); // Change car or seats quantity button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_COMMENTARY_CALLBACK)); // Tomorrow button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private EditMessageText editBeforeSaveSettlementLocationMessage(Message incomeMessage) {
        List<Pair<String, String>> buttonsAttributesList = createEditSettlementsLocationButtonsAttributesList();
        editMessage = createEditSettlementLocationMessage(incomeMessage, buttonsAttributesList);
        log.debug("method: EditBeforeSaveSettlementLocationMessage");
        return editMessage;
    }

    private List<Pair<String, String>> createEditSettlementsLocationButtonsAttributesList() {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.swapDepartureDestinationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_SWAP_DEPARTURE_DESTINATION_CALLBACK.getValue())); // Swap departure and destination button
        buttonsAttributesList.add(buttons.departureSettlementButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DEPARTURE_SETTLEMENT_CALLBACK.getValue())); // Edit departure settlement button
        buttonsAttributesList.add(buttons.destinationSettlementButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DESTINATION_SETTLEMENT_CALLBACK.getValue())); // Edit departure location button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private EditMessageText editBeforeSaveDateTimeMessage(Message incomeMessage) {
        String callbackDate = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DATE_CALLBACK.getValue();
        String callbackTime = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_TIME_CALLBACK.getValue();
        editMessage = createDateTimeMessage(incomeMessage, callbackDate, callbackTime);
        log.debug("method: EditBeforeSaveDateTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDepartureSettlementMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DEPARTURE_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseOfAllSettlementsMessage(incomeMessage, messageText, callback);
        log.debug("method: EditBeforeSaveDepartureSettlementMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDestinationSettlementMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseOfAllSettlementsMessage(incomeMessage, messageText, callback);
        log.debug("method: EditBeforeSaveDestinationSettlementMessage");
        return editMessage;
    }

    private void editBeforeSaveSwapDepartureDestination(long chatId) {
        log.debug("method: editBeforeSaveSwapDepartureDestination");
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId);
        if (dto.getDirection().equals(Direction.FROM_MINSK.getValue())) {
            dto.setDirection(Direction.TOWARDS_MINSK.getValue());
        } else {
            dto.setDirection(Direction.FROM_MINSK.getValue());
        }
        Settlement settlement = dto.getDepartureSettlement();
        dto.setDepartureSettlement(dto.getDestinationSettlement());
        dto.setDestinationSettlement(settlement);
        findRideDtoOperations.update(chatId, dto);
        log.debug("method: editBeforeSaveSwapDepartureDestination");
    }

    private EditMessageText editBeforeSaveChangeDateMessage(Message incomeMessage) {
        String todayCallback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TODAY;
        String tomorrowCallback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TOMORROW;
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: editBeforeSaveChangeDateMessage");
        return editMessage;
    }

    private void editBeforeSaveTimeSendMessage(long chatId) {
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_STATUS.getValue();
        createTimeSendMessage(chatId, callback);
        log.debug("method: editBeforeSaveTimeMessage");
    }

    private EditMessageText editBeforeSaveTimeMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_FIND_RIDE_REQUEST_TIME_MESSAGE();
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, messageText, chatStatus);
        log.debug("method: editBeforeSaveTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveSeatsMessage(Message incomeMessage) {
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_SEATS_QUANTITY_STATUS.getValue();
        editMessage = createSeatsMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveSeatsMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveCommentaryMessage(Message incomeMessage) {
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_COMMENTARY_STATUS.getValue();
        createCommentaryMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveCommentaryMessage");
        return editMessage;
    }

    private EditMessageText chooseRequestToEditMessage(Message incomeMessage) {
        String message = messages.getCHOOSE_REQUEST_TO_EDIT_MESSAGE() + requestListToString(incomeMessage.getChatId());
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_REQUEST_START_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(callback, incomeMessage.getChatId());
        editMessage = createChoiceRequestMessage(incomeMessage, message, buttonsAttributesList);
        log.debug("method: chooseRequestToEditMessage");
        return editMessage;
    }

    private EditMessageText startEditRequestMessage(Message incomeMessage, int requestId) {
        editMessage = createStartEditRequestMessage(incomeMessage, createEditButtonsAttributesList(requestId));
        log.debug("method: startEditRequestMessage");
        return editMessage;
    }

    private List<Pair<String, String>> createEditButtonsAttributesList(int requestId) {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.settlementLocationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_SETTLEMENT_LOCATION_CALLBACK.getValue() + requestId)); // Edit settlements or locations button
        buttonsAttributesList.add(buttons.dateTimeButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_DATE_TIME_CALLBACK.getValue() + requestId)); // Edit date or time button
        buttonsAttributesList.add(buttons.seatsQuantityButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_SEATS_QUANTITY_CALLBACK.getValue() + requestId)); // Change car or seats quantity button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_COMMENTARY_CALLBACK.getValue() + requestId)); // Edit commentary button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private EditMessageText editSettlementLocationMessage(Message incomeMessage, int requestId) {
        List<Pair<String, String>> buttonsAttributesList = createEditSettlementsLocationButtonsAttributesList(requestId);
        editMessage = createEditSettlementLocationMessage(incomeMessage, buttonsAttributesList);
        log.debug("method: editSettlementLocationMessage");
        return editMessage;
    }

    private List<Pair<String, String>> createEditSettlementsLocationButtonsAttributesList(int requestId) {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.swapDepartureDestinationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_SWAP_DEPARTURE_DESTINATION_CALLBACK.getValue() + requestId)); // Swap departure and destination button
        buttonsAttributesList.add(buttons.departureSettlementButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_DEPARTURE_SETTLEMENT_CALLBACK.getValue() + requestId)); // Edit departure settlement button
        buttonsAttributesList.add(buttons.destinationSettlementButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_DESTINATION_SETTLEMENT_CALLBACK.getValue() + requestId)); // Edit departure location button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private FindRideRequest editSwapDepartureDestination(int requestId) {
        log.debug("method: editSwapDepartureDestination");
        FindRideRequest request = findRideRequestService.findById(requestId);
        if (request.getDirection().equals(Direction.FROM_MINSK.getValue())) {
            request.setDirection(Direction.TOWARDS_MINSK.getValue());
        } else {
            request.setDirection(Direction.FROM_MINSK.getValue());
        }
        Settlement settlement = request.getDepartureSettlement();
        request.setDepartureSettlement(request.getDestinationSettlement());
        request.setDestinationSettlement(settlement);
        findRideRequestService.updateRequest(request);
        log.debug("method: editBeforeSaveSwapDepartureDestination");
        return request;
    }

    private EditMessageText editDepartureSettlementMessage(Message incomeMessage, int requestId) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callbackData =
                handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_DEPARTURE_SETTLEMENT_CALLBACK.getValue();
        return createEditSettlementMessage(incomeMessage, messageText, callbackData, requestId);
    }

    private EditMessageText editDestinationSettlementMessage(Message incomeMessage, int requestId) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callbackData =
                handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        return createEditSettlementMessage(incomeMessage, messageText, callbackData, requestId);
    }

    private FindRideRequest setEditedDepartureSettlement(int requestId, int settlementId) {
        log.debug("method: setEditedDepartureSettlement");
        FindRideRequest request = findRideRequestService.findById(requestId);
        request.setDepartureSettlement(settlementService.findById(settlementId));
        return findRideRequestService.updateRequest(request);
    }

    private FindRideRequest setEditedDestinationSettlement(int requestId, int settlementId) {
        log.debug("method: setEditedDestinationSettlement");
        FindRideRequest request = findRideRequestService.findById(requestId);
        request.setDestinationSettlement(settlementService.findById(settlementId));
        return findRideRequestService.updateRequest(request);
    }

    private EditMessageText editDateTimeMessage(Message incomeMessage, int requestId) {
        String callbackDate = handlerPrefix + FindPassengerRequestOperation.EDIT_DATE_CALLBACK.getValue() + requestId;
        String callbackTime = handlerPrefix + FindPassengerRequestOperation.EDIT_TIME_CALLBACK.getValue() + requestId;
        editMessage = createDateTimeMessage(incomeMessage, callbackDate, callbackTime);
        log.debug("method: editDateTimeMessage");
        return editMessage;
    }

    private EditMessageText editDateMessage(Message incomeMessage, int requestId) {
        String todayCallback = handlerPrefix
                + String.format(FindPassengerRequestOperation.EDIT_CHANGE_DATE_CALLBACK.getValue(), Day.TODAY.getValue(), requestId);
        String tomorrowCallback = handlerPrefix
                + String.format(FindPassengerRequestOperation.EDIT_CHANGE_DATE_CALLBACK.getValue(), Day.TOMORROW.getValue(), requestId);
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: editBeforeSaveChangeDateMessage");
        return editMessage;
    }

    private FindRideRequest editSetDate(int requestId, String day) {
        log.debug("method editSetDate");
        FindRideRequest request = findRideRequestService.findById(requestId);
        LocalDateTime rideBefore = request.getDepartureBefore();
        if (isToday(day)) {
            request.setDepartureBefore(rideBefore.withDayOfMonth(LocalDate.now().getDayOfMonth()));
        } else {
            request.setDepartureBefore(rideBefore.withDayOfMonth(LocalDate.now().getDayOfMonth() + 1));
        }
        return findRideRequestService.updateRequest(request);
    }

    private EditMessageText editTimeMessage(Message incomeMessage, int requestId) {
        String messageText = messages.getCREATE_FIND_RIDE_REQUEST_TIME_MESSAGE();
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_TIME_STATUS.getValue() + requestId;
        editMessage = createTimeMessage(incomeMessage, messageText, chatStatus);
        log.debug("method: editTimeMessage");
        return editMessage;
    }

    private void editTimeSendValidTimeMessage(long chatId, int requestId) {
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_TIME_STATUS.getValue() + requestId;
        createTimeSendMessage(chatId, callback);
        log.debug("method: editTimeSendValidTimeMessage");
    }

    private FindRideRequest editSetTime(int requestId, LocalTime time) {
        log.debug("method editSetTime");
        FindRideRequest request = findRideRequestService.findById(requestId);
        request.setDepartureBefore(request.getDepartureBefore().withHour(time.getHour()).withMinute(time.getMinute()));
        return findRideRequestService.updateRequest(request);
    }

    private EditMessageText editSeatsMessage(Message incomeMessage, int requestId) {
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_SEATS_QUANTITY_STATUS.getValue() + requestId;
        editMessage = createSeatsMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveSeatsMessage");
        return editMessage;
    }

    private FindRideRequest setEditedSeatsQuantity(int requestId, int quantity) {
        log.debug("method: setEditedSeatsQuantity");
        FindRideRequest request = findRideRequestService.findById(requestId);
        request.setPassengersQuantity(quantity);
        return findRideRequestService.updateRequest(request);
    }

    private EditMessageText editCommentaryMessage(Message incomeMessage, int requestId) {
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_COMMENTARY_STATUS.getValue() + requestId;
        createCommentaryMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveCommentaryMessage");
        return editMessage;
    }

    private FindRideRequest setEditedCommentary(int requestId, String commentary) {
        log.debug("method: setEditedSeatsQuantity");
        FindRideRequest request = findRideRequestService.findById(requestId);
        request.setCommentary(firstLetterToUpperCase(commentary));
        return findRideRequestService.updateRequest(request);
    }

    private EditMessageText editRequestSuccessEditMessage(Message incomeMessage, FindRideRequest request) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_SUCCESS_EDITION_MESSAGE()
                + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusOperations.deleteChatStatus(incomeMessage.getChatId());
        return editMessage;
    }

    private SendMessage editRequestSuccessSendMessage(long chatId, FindRideRequest request) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getFIND_PASSENGER_SUCCESS_EDITION_MESSAGE()
                + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusOperations.deleteChatStatus(chatId);
        return sendMessage;
    }

    private EditMessageText sendNecessityToCancelMessage(Message incomeMessage) {
        editMessage = createNecessityToCancelMessage(incomeMessage, handlerPrefix);
        log.debug("method: sendNecessityToCancelMessage");
        return editMessage;
    }

    private EditMessageText chooseRequestToCancelMessage(Message incomeMessage) {
//        TODO переделать как и в выводе списка для редактирования
        String message = messages.getCHOOSE_REQUEST_TO_CANCEL_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CANCEL_REQUEST_CALLBACK.getValue();
        editMessage = createChoiceRequestMessage(incomeMessage, message, callback);
        log.debug("method: chooseRequestToEditMessage");
        return editMessage;
    }

    private EditMessageText createChoiceRequestMessage(Message incomeMessage, String messageText, String callback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText + requestListToString(incomeMessage.getChatId()));
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(requestButtonsAttributesListCreator(callback, incomeMessage.getChatId())));
        log.debug("method: createChoiceRequestMessage");
        return editMessage;
    }

    private FindRideRequest cancelRequest(int requestId) {
        log.debug("method sendCancelRequest");
        return findRideRequestService.cancelRequestById(requestId);
    }

    private EditMessageText sendCancelRequestSuccessMessage(Message incomeMessage, FindRideRequest request) {
        String requestToString = requestToString(request);
        editMessage = createCancelRequestSuccessMessage(incomeMessage, requestToString);
        return editMessage;
    }

    public void sendExpireDepartureTimeMessage(int requestId) {
        FindRideRequest request = findRideRequestService.findById(requestId);
        sendMessage = createExpireRequestTimeMessage(request.getUser().getChatId(), requestToString(request));
        log.debug("method: sendExpireDepartureTimeMessage");
        sendBotMessage(sendMessage);
    }


    private FindRideRequest saveRequest(long chatId) {
        FindRideRequestDTO dto = findRideDtoOperations.getDTO(chatId);
        findRideDtoOperations.delete(chatId);
        chatStatusOperations.deleteChatStatus(chatId);
        log.debug("method saveRequest");
        FindRideRequest request = findRideRequestService.addNewRequest(dto);
        return request;
    }

    private FindRideRequest getLastRequest(long chatId) {
        return findRideRequestService.findLastUserRequest(chatId);
    }

    private Optional<FindRideRequest> getLastRequestOptional(long chatId) {
        return findRideRequestService.findLastUserRequestOptional(chatId);
    }

    private List<FindRideRequest> getUserActiveFindRideRequestsList(long chatId) {
        return findRideRequestService.usersActiveRequestList(chatId);
    }

    public List<Pair<String, String>> requestButtonsAttributesListCreator(String callbackData, long chatId) {
        List<FindRideRequest> requestList = getUserActiveFindRideRequestsList(chatId);
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
        List<FindRideRequest> requests = getUserActiveFindRideRequestsList(chatId);
        if (requests.isEmpty()) {
            return messages.getFIND_RIDE_NO_ACTIVE_REQUEST_MESSAGE();
        } else {
            StringBuilder text = new StringBuilder();
            for (FindRideRequest request : requests) {
                int n = requests.indexOf(request) + 1;
                text.append(n).append(". ").append(requestToString(request)).append("\n");
            }
            return text.toString();
        }
    }

    public String requestToString(Optional<FindRideRequest> optional) {
        String messageText;
        if (optional.isPresent()) {
            messageText = requestToString(optional.get());
        } else messageText = messages.getFIND_PASSENGER_NO_ACTIVE_REQUEST_MESSAGE();
        return messageText;
    }

    private String dtoToString(FindRideRequestDTO dto) {
        String messageText = String.format(messages.getCREATE_FIND_RIDE_REQUEST_CHECK_DATA_BEFORE_SAVE_MESSAGE(),
                dto.getUser().getFirstName(),
                dto.getDepartureSettlement().getName(),
                dto.getDestinationSettlement().getName(),
                dto.getDepartureBefore().toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                dto.getDepartureBefore().toLocalTime().toString(),
                dto.getPassengersQuantity(),
                dto.getCommentary());
        return messageText;
    }

    public String requestToString(FindRideRequest request) {
        String messageText = String.format(messages.getFIND_RIDE_REQUEST_TO_STRING_MESSAGE(),
                request.getUser().getFirstName(),
                request.getDepartureSettlement().getName(),
                request.getDestinationSettlement().getName(),
                request.getDepartureBefore().toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                request.getDepartureBefore().toLocalTime().toString(),
                request.getPassengersQuantity(),
                request.getCommentary(),
                request.getCreatedAt().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
        return messageText;
    }

    private boolean isExpired(long chatId, LocalTime time) {
        return findRideDtoOperations.getDTO(chatId).getDepartureBefore().withHour(time.getHour()).withMinute(time.getMinute()).isBefore(LocalDateTime.now());
    }

    private boolean isExpired(long chatId) {
        return findRideDtoOperations.getDTO(chatId).getDepartureBefore().isBefore(LocalDateTime.now());
    }

    private boolean isExpired(int requestId) {
        return findRideRequestService.findById(requestId).getDepartureBefore().isBefore(LocalDateTime.now());
    }

    private boolean isExpired(int requestId, LocalTime time) {
        return findRideRequestService.findById(requestId)
                .getDepartureBefore().withHour(time.getHour()).withMinute(time.getMinute())
                .isBefore(LocalDateTime.now());
    }

    private boolean isRequestQuantityLimit(long chatId) {
        return getUserActiveRequestsList(chatId).size() > 2;
    }

    private List<FindRideRequest> getUserActiveRequestsList(long chatId) {
        return findRideRequestService.usersActiveRequestList(chatId);
    }
}

