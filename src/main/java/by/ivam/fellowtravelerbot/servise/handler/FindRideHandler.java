package by.ivam.fellowtravelerbot.servise.handler;


import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.bot.enums.Day;
import by.ivam.fellowtravelerbot.bot.enums.Direction;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.requestOperation;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.storages.interfaces.FindRideDTOStorageAccess;
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


// This class handle operations with search of hitchhiker's rides
@Service
@Data
@Log4j
public class FindRideHandler extends RequestHandler implements HandlerInterface {
    @Autowired
    private final FindRideDTOStorageAccess storageAccess;
    @Autowired
    private final AdminHandler adminHandler;
    private final String handlerPrefix = Handlers.FIND_RIDE.getHandlerPrefix();
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
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
                int requestId = trimId(chatStatus);
                if (time.toNanoOfDay() == 100 || isExpired(requestId, time)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
//                    FindPassengerRequest request = editSetTime(requestId, time);
//                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                }
            }
            case "EDIT_CHANGE_SEATS_QUANTITY" -> {
                if (seatsQuantityIsValid(messageText)) {
//                    FindPassengerRequest request = setEditedSeatsQuantity(trimId(chatStatus), Integer.parseInt(messageText));
//                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "EDIT_CHANGE_COMMENTARY" -> {
//                FindPassengerRequest request = setEditedCommentary(trimId(chatStatus), messageText);
//                if (messageText.length() >= 1000) sendMessage = nextStep(chatId);
//                else sendMessage = editRequestSuccessSendMessage(chatId, request);
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
            process = trimProcess(callback);
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
                String direction = trimSecondSubstring(callback);
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
                int settlementId = trimId(callback);
                if (settlementId == -1) {
                    editMessage = createNewRequestChooseAnotherSettlementAsDepartureMessage(incomeMessage);
                } else {
                    setDTODepartureSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDateMessage(incomeMessage);
                }
            }
            case "CREATE_REQ_DEST_SETTLEMENT" -> {
                int settlementId = trimId(callback);
                if (settlementId == -1) {
                    editMessage = createNewRequestChooseAnotherSettlementAsDestinationMessage(incomeMessage);
                } else {
                    setDtoDestinationSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDateMessage(incomeMessage);
                }
            }
            case "CREATE_REQUEST_DATE" -> {
                String day = trimSecondSubstring(callback);
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
                setDTODepartureSettlement(chatId, trimId(callback));
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEST_SETTLEMENT" -> {
                editMessage = editBeforeSaveDestinationSettlementMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEST_SETTLEMENT" -> {
                setDtoDestinationSettlement(chatId, trimId(callback));
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
                FindRideRequest request = cancelRequest(trimId(callback));
                editMessage = cancelRequestSuccessMessage(incomeMessage, request);
            }
            case "CHOOSE_REQUEST_TO_EDIT" -> {
                editMessage = chooseRequestToEditMessage(incomeMessage);
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
        storageAccess.addFindRideDTO(chatId, requestDTO);
        log.debug("method: createFindPassengerRequestDTO - create DTO " + dto + " and save it in storage");
    }

    private EditMessageText createNewRequestChoseDirectionMessage(Message incomeMessage) {
        editMessage = createChoseDirectionMessage(incomeMessage, handlerPrefix);
        log.debug("method: createNewRequestChoseDirectionMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseResidenceAsDepartureMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + requestOperation.CREATE_REQUEST_SETTLEMENT_DEPARTURE_CALLBACK.getValue();
        editMessage = createChooseResidenceMessage(incomeMessage, messageText, callback);
        log.debug("method: createNewRequestChooseResidenceAsDepartureMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseAnotherSettlementAsDepartureMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + requestOperation.CREATE_REQUEST_SETTLEMENT_DEPARTURE_CALLBACK.getValue();
        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        editMessage = createChooseAnotherSettlementMessage(incomeMessage, settlementList, messageText, callback);
        log.debug("method: createNewRequestChooseAnotherSettlementAsDepartureMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseResidenceAsDestinationMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + requestOperation.CREATE_REQUEST_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseResidenceMessage(incomeMessage, messageText, callback);
        log.debug("method: createNewRequestChooseResidenceAsDestinationMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseAnotherSettlementAsDestinationMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + requestOperation.CREATE_REQUEST_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        //        TODO вынести settlementList в createChooseAnotherSettlementMessage?
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        editMessage = createChooseAnotherSettlementMessage(incomeMessage, settlementList, messageText, callback);
        log.debug("method: createNewRequestChooseAnotherSettlementAsDestinationMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestChooseDateMessage(Message incomeMessage) {
        String todayCallback = handlerPrefix + requestOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TODAY;
        String tomorrowCallback = handlerPrefix + requestOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TOMORROW;
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: createNewRequestChooseDateMessage");
        return editMessage;
    }

    private EditMessageText createNewRequestTimeMessage(Message incomeMessage) {
//        TODO добавить  кнопки с промежутками времени если выезд сегодня.
        String messageText = messages.getCREATE_FIND_RIDE_REQUEST_TIME_MESSAGE();
        String chatStatus = handlerPrefix + requestOperation.CREATE_REQUEST_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, messageText, chatStatus);
        log.debug("method: createNewRequestTimeMessage");
        return editMessage;
    }

    private void setDTODirection(long chatId, String direction) {
        log.debug("method createNewRequestSetDirection");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId).setDirection(direction);
        storageAccess.update(chatId, dto);
    }

    private void setDTODepartureSettlement(long chatId, int settlementId) {
        log.debug("method setDTODepartureSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindRideRequestDTO dto = storageAccess.getDTO(chatId).setDepartureSettlement(settlement);
        storageAccess.update(chatId, dto);
    }

    private void setDtoDestinationSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDestinationSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindRideRequestDTO dto = storageAccess.getDTO(chatId).setDestinationSettlement(settlement);
        storageAccess.update(chatId, dto);
    }

    private void setDtoDate(long chatId, String day) {
        log.debug("method createNewRequestSetDate");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId);
        if (isToday(day)) {
            dto.setDepartureBefore(LocalDate.now().atTime(0, 0));
        } else {
            dto.setDepartureBefore(LocalDate.now().atTime(0, 0).plusDays(1));
        }
        storageAccess.update(chatId, dto);
    }

    private void setEditedDtoDate(long chatId, String day) {
        log.debug("method createNewRequestSetDate");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId);
        if (isToday(day)) {
            dto.setDepartureBefore(dto.getDepartureBefore().withDayOfMonth(LocalDate.now().getDayOfMonth()));
        } else {
            dto.setDepartureBefore(dto.getDepartureBefore().withDayOfMonth(LocalDate.now().getDayOfMonth()).plusDays(1));
        }
        storageAccess.update(chatId, dto);
    }

    private void setDtoTime(long chatId, LocalTime time) {
        log.debug("method createNewRequestSetTime");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId);
        dto.setDepartureBefore(dto.getDepartureBefore().withHour(time.getHour()).withMinute(time.getMinute()));
        storageAccess.update(chatId, dto);
    }

    private SendMessage createNewRequestSeatsMessage(long chatId) {
        String chatStatus = handlerPrefix + requestOperation.CREATE_REQUEST_SEATS_STATUS.getValue();
        sendMessage = createSeatsMessage(chatId, chatStatus);
        log.debug("method: createNewRequestSeatsMessage");
        return sendMessage;
    }

    private void setDtoPassengersQuantity(long chatId, int passengersQuantity) {
        log.debug("method setDtoPassengersQuantity");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId).setPassengersQuantity(passengersQuantity);
        storageAccess.update(chatId, dto);
    }

    private SendMessage createNewRequestCommentaryMessage(long chatId) {
        sendMessage = createCommentaryMessage(chatId, handlerPrefix);
        log.debug("method: createNewRequestCommentaryMessage");
        return sendMessage;
    }

    private void setDtoCommentary(long chatId, String commentary) {
        log.debug("method setDtoCommentary");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId).setCommentary(firstLetterToUpperCase(commentary));
        storageAccess.update(chatId, dto);
    }

    private EditMessageText checkDataBeforeSaveMessageSkipComment(Message incomeMessage) {
        log.debug("method checkDataBeforeSaveMessageSkipComment");
        String messageText = dtoToString(storageAccess.getDTO(incomeMessage.getChatId()));
        editMessage = createCheckDataBeforeSaveMessageSkipComment(incomeMessage, messageText, handlerPrefix);
        return editMessage;
    }

    private SendMessage checkDataBeforeSaveMessage(long chatId) {
        log.debug("method checkDataBeforeSaveMessage");
        String messageText = dtoToString(storageAccess.getDTO(chatId));
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
        editMessage = createStartEditRequestMessage(incomeMessage, createEditBeforeSaveuttonsAttributesList(handlerPrefix));
        log.debug("method: startEditBeforeSaveRequestMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveSettlementLocationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_REQUEST_START_EDIT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.swapDepartureDestinationButtonCreate(handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_SWAP_DEPARTURE_DESTINATION_CALLBACK.getValue())); // Swap departure and destination button
        buttonsAttributesList.add(buttons.departureSettlementButtonCreate(handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_DEPARTURE_SETTLEMENT_CALLBACK.getValue())); // Edit departure settlement button
        buttonsAttributesList.add(buttons.destinationSettlementButtonCreate(handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_DESTINATION_SETTLEMENT_CALLBACK.getValue())); // Edit departure location button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveSettlementLocationMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDateTimeMessage(Message incomeMessage) {
        String callbackDate = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_DATE_CALLBACK.getValue();
        String callbackTime = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_TIME_CALLBACK.getValue();
        editMessage = createDateTimeMessage(incomeMessage, callbackDate, callbackTime);
        log.debug("method: EditBeforeSaveDateTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDepartureSettlementMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_CHANGE_DEPARTURE_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseOfAllSettlementsMessage(incomeMessage, messageText, callback);
        log.debug("method: EditBeforeSaveDepartureSettlementMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDestinationSettlementMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_CHANGE_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseOfAllSettlementsMessage(incomeMessage, messageText, callback);
        log.debug("method: EditBeforeSaveDestinationSettlementMessage");
        return editMessage;
    }

    private void editBeforeSaveSwapDepartureDestination(long chatId) {
        log.debug("method: editBeforeSaveSwapDepartureDestination");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId);
        if (dto.getDirection().equals(Direction.FROM_MINSK.getValue())) {
            dto.setDirection(Direction.TOWARDS_MINSK.getValue());
        } else {
            dto.setDirection(Direction.FROM_MINSK.getValue());
        }
        Settlement settlement = dto.getDepartureSettlement();
        dto.setDepartureSettlement(dto.getDestinationSettlement());
        dto.setDestinationSettlement(settlement);
        storageAccess.update(chatId, dto);
    }

    private EditMessageText editBeforeSaveChangeDateMessage(Message incomeMessage) {
        String todayCallback = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TODAY;
        String tomorrowCallback = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_CHANGE_DATE_CALLBACK.getValue() + Day.TOMORROW;
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: editBeforeSaveChangeDateMessage");
        return editMessage;
    }

    private void editBeforeSaveTimeSendMessage(long chatId) {
        String callback = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_STATUS.getValue();
        createTimeSendMessage(chatId, callback);
        log.debug("method: editBeforeSaveTimeMessage");
    }

    private EditMessageText editBeforeSaveTimeMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE();
        String chatStatus = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, messageText, chatStatus);
        log.debug("method: editBeforeSaveTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveSeatsMessage(Message incomeMessage) {
        String chatStatus = handlerPrefix + requestOperation.EDIT_BEFORE_SAVE_CHANGE_SEATS_QUANTITY_STATUS.getValue();
        editMessage = createSeatsMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveSeatsMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveCommentaryMessage(Message incomeMessage) {
        String chatStatus = handlerPrefix + requestOperation.CREATE_REQUEST_COMMENTARY_STATUS.getValue();
        createCommentaryMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveCommentaryMessage");
        return editMessage;
    }

    private EditMessageText chooseRequestToEditMessage(Message incomeMessage) {
        String message = messages.getCHOOSE_REQUEST_TO_EDIT_MESSAGE();
        String callback = handlerPrefix + requestOperation.EDIT_REQUEST_START_CALLBACK.getValue();
        editMessage = createChoiceRequestMessage(incomeMessage, message, callback);
        log.debug("method: chooseRequestToEditMessage");
        return editMessage;
    }
    private EditMessageText startEditRequestMessage(Message incomeMessage, int requestId) {
        editMessage = createStartEditRequestMessage(incomeMessage, createEditBeforeSaveuttonsAttributesList(handlerPrefix, requestId));
        log.debug("method: startEditRequestMessage");
        return editMessage;
    }

    private EditMessageText sendNecessityToCancelMessage(Message incomeMessage) {
        editMessage = createNecessityToCancelMessage(incomeMessage, handlerPrefix);
        log.debug("method: sendNecessityToCancelMessage");
        return editMessage;
    }

    private EditMessageText chooseRequestToCancelMessage(Message incomeMessage) {
        String message = messages.getCHOOSE_REQUEST_TO_CANCEL_MESSAGE();
        String callback = handlerPrefix + requestOperation.CANCEL_REQUEST_CALLBACK.getValue();
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
        FindRideRequest request = findRideRequestService.findById(requestId);
        request.setActive(false)
                .setCanceled(true)
                .setCanceledAt(LocalDateTime.now());
        log.debug("method cancelRequest");
        return findRideRequestService.updateRequest(request);
    }

    private EditMessageText cancelRequestSuccessMessage(Message incomeMessage, FindRideRequest request) {
        String requestToString = requestToString(request);
        editMessage = createCancelRequestSuccessMessage(incomeMessage, requestToString);
        return editMessage;
    }

    private FindRideRequest saveRequest(long chatId) {
        FindRideRequestDTO dto = storageAccess.getDTO(chatId);
        storageAccess.delete(chatId);
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("method saveRequest");
        return findRideRequestService.addNewRequest(dto);
    }

    private FindRideRequest getLastRequest(long chatId) {
        return findRideRequestService.findLastUserRequest(chatId);
    }

    private Optional<FindRideRequest> getLastRequestOptional(long chatId) {
        return findRideRequestService.findLastUserRequestOptional(chatId);
    }

    private List<FindRideRequest> getUserActiveFindPassengerRequestsList(long chatId) {
        return findRideRequestService.usersActiveRequestList(chatId);
    }

    public List<Pair<String, String>> requestButtonsAttributesListCreator(String callbackData, long chatId) {
        List<FindRideRequest> requestList = getUserActiveFindPassengerRequestsList(chatId);
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
        List<FindRideRequest> requests = getUserActiveFindPassengerRequestsList(chatId);
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
        return storageAccess.getDTO(chatId).getDepartureBefore().withHour(time.getHour()).withMinute(time.getMinute()).isBefore(LocalDateTime.now());
    }

    private boolean isExpired(long chatId) {
        return storageAccess.getDTO(chatId).getDepartureBefore().isBefore(LocalDateTime.now());
    }

    private boolean isExpired(int requestId) {
        return findPassengerRequestService.findById(requestId).getDepartureAt().isBefore(LocalDateTime.now());
    }

    private boolean isExpired(int requestId, LocalTime time) {
        return findPassengerRequestService.findById(requestId).getDepartureAt().toLocalDate().isEqual(LocalDate.now()) && time.isBefore(LocalTime.now());
    }

    private boolean isRequestQuantityLimit(long chatId) {
        return getUserActiveRequestsList(chatId).size() > 2;
    }

    private List<FindRideRequest> getUserActiveRequestsList(long chatId) {
        return findRideRequestService.usersActiveRequestList(chatId);
    }


}

