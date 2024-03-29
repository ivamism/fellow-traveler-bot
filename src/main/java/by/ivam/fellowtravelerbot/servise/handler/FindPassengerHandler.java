package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.bot.enums.*;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.Location;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.DTO.stateOperations.interfaces.FindPassengerDtoOperations;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
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
@EqualsAndHashCode(callSuper = true)
@Service
@Data
@Log4j2
public class FindPassengerHandler extends RequestHandler implements HandlerInterface {
    private final String handlerPrefix = Handlers.FIND_PASSENGER.getHandlerPrefix();
    @Autowired
    private FindPassengerDtoOperations dtoOperations;

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        String messageText = incomeMessage.getText();
        Long chatId = incomeMessage.getChatId();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus + ". message: " + messageText);
        String process = chatStatus;
        if (chatStatus.contains(":")) {
            process = extractProcess(chatStatus);
        }
        switch (process) {
            case "CREATE_REQUEST_TIME_STATUS" -> {
                LocalTime time = getTime(messageText);
                if (isACorrectTimeMessageOrTime(time, chatId)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    createNewRequestSetTime(chatId, time);
                    sendMessage = createNewRequestChooseCarMessage(chatId);
                }
            }
            case "CREATE_REQUEST_SEATS_STATUS" -> {
                if (seatsQuantityIsValid(messageText)) {
                    setDtoSeatsQuantity(chatId, Integer.parseInt(messageText));
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
                if (isACorrectTimeMessageOrTime(time, chatId)) {
                    sendMessage = handleReceivedIncorrectTime(time, chatId);
                } else {
                    createNewRequestSetTime(chatId, time);
                    sendMessage = checkDataBeforeSaveMessage(chatId);
                }
            }
            case "EDIT_BEFORE_SAVE_CHANGE_SEATS_QUANTITY_STATUS" -> {
                if (seatsQuantityIsValid(messageText)) {
                    setDtoSeatsQuantity(chatId, Integer.parseInt(messageText));
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
                    FindPassengerRequest request = editSetTime(requestId, time);
                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                }
            }
            case "EDIT_CHANGE_SEATS_QUANTITY" -> {
                if (seatsQuantityIsValid(messageText)) {
                    FindPassengerRequest request = setEditedSeatsQuantity(extractId(chatStatus, getFIRST_VALUE()), Integer.parseInt(messageText));
                    sendMessage = editRequestSuccessSendMessage(chatId, request);
                } else {
                    sendMessage = invalidSeatsQuantityFormatMessage(chatId);
                }
            }
            case "EDIT_CHANGE_COMMENTARY" -> {
                FindPassengerRequest request = setEditedCommentary(extractId(chatStatus, getFIRST_VALUE()), messageText);
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
                    createFindPassengerRequestDTO(chatId);
                    editMessage = createNewRequestChoseDirectionMessage(incomeMessage);
                }
            }
            case "CREATE_REQUEST_DIRECTION" -> {
                String direction = extractParameter(callback, getFIRST_VALUE());
                setDTODirection(chatId, direction);
                if (direction.equals(String.valueOf(Direction.FROM_MINSK))) {
                    int settlementId = settlementService.findByName("Минск").getId();
                    setDTODepartureSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDepartureLocationMessage(incomeMessage, settlementId);
                } else if ((direction.equals(String.valueOf(Direction.TOWARDS_MINSK)))) {
                    editMessage = createNewRequestChooseResidenceAsDepartureMessage(incomeMessage);
                }
            }
            case "CREATE_REQ_DEP_SETTLEMENT" -> {
                int settlementId = extractId(callback, getFIRST_VALUE());
                if (settlementId == -1) {
                    editMessage = createNewRequestChooseAnotherSettlementAsDepartureMessage(incomeMessage);
                } else {
                    setDTODepartureSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDepartureLocationMessage(incomeMessage, settlementId);
                }
            }

            case "CREATE_REQUEST_DEP_LOCATION" -> {
                createNewRequestSetDepartureLocation(chatId, extractId(callback, getFIRST_VALUE()));
                if (dtoOperations.getDTO(chatId).getDirection().equals(String.valueOf(Direction.TOWARDS_MINSK))) {
                    int settlementId = settlementService.findByName("Минск").getId();
                    setDtoDestinationSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDestinationLocationMessage(incomeMessage, settlementId);
                } else if ((dtoOperations.getDTO(chatId).getDirection().equals(String.valueOf(Direction.FROM_MINSK)))) {
                    editMessage = createNewRequestChooseResidenceAsDestinationMessage(incomeMessage);
                }
            }
            case "CREATE_REQ_DEST_SETTLEMENT" -> {
                int settlementId = extractId(callback, getFIRST_VALUE());
                if (settlementId == -1) {
                    editMessage = createNewRequestChooseAnotherSettlementAsDestinationMessage(incomeMessage);
                } else {
                    setDtoDestinationSettlement(chatId, settlementId);
                    editMessage = createNewRequestChooseDestinationLocationMessage(incomeMessage, settlementId);
                }
            }
            case "CREATE_REQUEST_DEST_LOCATION" -> {
                createNewRequestSetDestinationLocation(chatId, extractId(callback, getFIRST_VALUE()));
                editMessage = createNewRequestChooseDateMessage(incomeMessage);
            }
            case "CREATE_REQUEST_DATE" -> {
                String day = extractParameter(callback, getFIRST_VALUE());
                setDtoDate(chatId, day);
                editMessage = createNewRequestTimeMessage(incomeMessage);
            }
            case "CREATE_REQUEST_CAR_CALLBACK" -> {
                setDtoCar(chatId, extractId(callback, getFIRST_VALUE()));
                editMessage = createNewRequestSeatsMessage(incomeMessage);
            }
            case "CREATE_REQUEST_SKIP_COMMENT_CALLBACK" -> {
                setDtoCommentary(chatId, "-");
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "SAVE_REQUEST_CALLBACK" -> {
                FindPassengerRequest request = saveRequest(chatId);
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
            case "EDIT_BEFORE_SAVE_CAR_DETAILS" -> {
                editMessage = editBeforeSaveCarDetailsMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEP_SETTLEMENT" -> {
                editMessage = editBeforeSaveDepartureSettlementMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEP_SETTLEMENT" -> {
                setDTODepartureSettlement(chatId, extractId(callback, getFIRST_VALUE()));
                editMessage = editBeforeSaveDepartureLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEP_LOCATION" -> {
                editMessage = editBeforeSaveDepartureLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEP_LOCATION" -> {
                createNewRequestSetDepartureLocation(chatId, extractId(callback, getFIRST_VALUE()));
                editMessage = checkDataBeforeSaveMessageSkipComment(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEST_SETTLEMENT" -> {
                editMessage = editBeforeSaveDestinationSettlementMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEST_SETTLEMENT" -> {
                setDtoDestinationSettlement(chatId, extractId(callback, getFIRST_VALUE()));
                editMessage = editBeforeSaveDestinationLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_DEST_LOCATION" -> {
                editMessage = editBeforeSaveDestinationLocationMessage(incomeMessage);
            }
            case "EDIT_BEFORE_SAVE_CHANGE_DEST_LOCATION" -> {
                createNewRequestSetDestinationLocation(chatId, extractId(callback, getFIRST_VALUE()));
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
                setDtoDate(chatId, day);
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
                setDtoCar(chatId, extractId(callback, getFIRST_VALUE()));
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
                editMessage = startEditRequestMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "NO_ACTIVE_REQUEST" -> {
                editMessage = noActiveRequestsMessage(incomeMessage);
            }
            case "EDIT_SETTLEMENT_LOCATION" -> {
                editMessage = editSettlementLocationMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_SWAP_DEP_DEST" -> {
                FindPassengerRequest request = editSwapDepartureDestination(extractId(callback, getFIRST_VALUE()));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DEP_SETTLEMENT" -> {
                editMessage = editDepartureSettlementMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_CHANGE_DEP_SETTLEMENT" -> {
                setEditedDepartureSettlement(extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
                editMessage = editDepartureLocationMessage(incomeMessage, extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
            }
            case "EDIT_DEST_SETTLEMENT" -> {
                editMessage = editDestinationSettlementMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_CHANGE_DEST_SETTLEMENT" -> {
                setEditedDestinationSettlement(extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
                editMessage = editDestinationLocationMessage(incomeMessage, extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
            }
            case "EDIT_DEP_LOCATION" -> {
                int requestId = extractId(callback, getFIRST_VALUE());
                int settlementId = findPassengerRequestService.findById(requestId).getDepartureSettlement().getId();
                editMessage = editDepartureLocationMessage(incomeMessage, extractId(callback, getFIRST_VALUE()), settlementId);
            }
            case "EDIT_CHANGE_DEP_LOCATION" -> {
                FindPassengerRequest request = setEditedDepartureLocation(extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_DEST_LOCATION" -> {
                int requestId = extractId(callback, getFIRST_VALUE());
                int settlementId = findPassengerRequestService.findById(requestId).getDestinationSettlement().getId();
                editMessage = editDestinationLocationMessage(incomeMessage, requestId, settlementId);
            }
            case "EDIT_CHANGE_DEST_LOCATION" -> {
                FindPassengerRequest request = setEditedDestinationLocation(extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
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
                FindPassengerRequest request = editSetDate(extractId(callback, getSECOND_VALUE()), extractParameter(callback, getFIRST_VALUE()));
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
            case "EDIT_CAR_DETAILS" -> {
                editMessage = editCarDetailsMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_CAR" -> {
                editMessage = editChooseCarMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_CHANGE_CAR" -> {
                FindPassengerRequest request = setEditedCar(extractId(callback, getFIRST_VALUE()), extractId(callback, getSECOND_VALUE()));
                editMessage = editRequestSuccessEditMessage(incomeMessage, request);
            }
            case "EDIT_SEATS_QUANTITY" -> {
                editMessage = editSeatsMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "EDIT_COMMENTARY" -> {
                editMessage = editCommentaryMessage(incomeMessage, extractId(callback, getFIRST_VALUE()));
            }
            case "CHOOSE_REQUEST_TO_CANCEL_CALLBACK" -> {
                editMessage = chooseRequestToCancelMessage(incomeMessage);
            }
            case "CANCEL_REQUEST" -> {
                FindPassengerRequest request = onCancelingRequestReceived(extractId(callback, getFIRST_VALUE()));
                editMessage = sendCancelRequestSuccessMessage(incomeMessage, request);
            }

        }
        sendEditMessage(editMessage);
    }

    public void startCreateNewRequest(long chatId) {
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_START_PROCESS_MESSAGE();
        sendMessage = createNewRequest(chatId, messageText, handlerPrefix);
        log.debug("method: startCreateNewRequest");
        sendBotMessage(sendMessage);
    }

    private void createFindPassengerRequestDTO(long chatId) {
        FindPassengerRequestDTO findPassengerRequestDTO = new FindPassengerRequestDTO();
        findPassengerRequestDTO.setUser(userService.findUserById(chatId));
        dtoOperations.addFindPassengerDTO(chatId, findPassengerRequestDTO);
        log.debug("method: createFindPassengerRequestDTO - create DTO " + findPassengerRequestDTO);
    }

    private EditMessageText createNewRequestChoseDirectionMessage(Message incomeMessage) {
        editMessage = createChoseDirectionMessage(incomeMessage, handlerPrefix);
        log.debug("method: createNewRequestChoseDirectionMessage");
        return editMessage;
    }

    private void setDTODirection(long chatId, String direction) {
        log.debug("method createNewRequestSetDirection");
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setDirection(direction);
        dtoOperations.update(chatId, dto);
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
//        TODO вынести settlementList в createChooseAnotherSettlementMessage?
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        editMessage = createChooseAnotherSettlementMessage(incomeMessage, settlementList, messageText, callback);
        log.debug("method: createNewRequestChooseAnotherSettlementAsDepartureMessage");
        return editMessage;
    }

    private void setDTODepartureSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDepartureSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setDepartureSettlement(settlement);
        dtoOperations.update(chatId, dto);
    }

    private EditMessageText createNewRequestChooseDepartureLocationMessage(Message incomeMessage, int settlementId) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_LOCATION_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DEP_LOCATION_CALLBACK.getValue();
        editMessage = createChooseLocationMessage(incomeMessage, settlementId, messageText, callback);
        log.debug("method: createNewRequestChooseDepartureLocationMessage");
        return editMessage;
    }

    private void createNewRequestSetDepartureLocation(long chatId, int locationId) {
        log.debug("method createNewRequestSetDepartureLocation");
        Location location = locationService.findById(locationId);
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setDepartureLocation(location);
        dtoOperations.update(chatId, dto);
    }

    private void setDtoDestinationSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDestinationSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setDestinationSettlement(settlement);
        dtoOperations.update(chatId, dto);
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

    // TODO сделать рефакторинг также как  и с местом выезда
    private EditMessageText createNewRequestChooseDestinationLocationMessage(Message incomeMessage, int settlementId) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_LOCATION_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DESTINATION_LOCATION_CALLBACK.getValue();
        editMessage = createChooseLocationMessage(incomeMessage, settlementId, messageText, callback);
        log.debug("method: createNewRequestChooseDestinationLocationMessage");
        return editMessage;
    }

    private void createNewRequestSetDestinationLocation(long chatId, int locationId) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");
        Location location = locationService.findById(locationId);
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setDestinationLocation(location);
        dtoOperations.update(chatId, dto);
    }

    private EditMessageText createNewRequestChooseDateMessage(Message incomeMessage) {
        String todayCallback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TODAY;
        String tomorrowCallback = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_DATE_CALLBACK.getValue() + Day.TOMORROW;
        editMessage = createChooseDateMessage(incomeMessage, todayCallback, tomorrowCallback);
        log.debug("method: createNewRequestChooseDateMessage");
        return editMessage;
    }

    private void setDtoDate(long chatId, String day) {
        log.debug("method createNewRequestSetDate");
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId);
        LocalDate rideDate = LocalDate.now();
        if (isToday(day)) {
            dto.setDepartureDate(rideDate);

        } else {
            dto.setDepartureDate(rideDate.plusDays(1));
        }
        dtoOperations.update(chatId, dto);
    }

    private EditMessageText createNewRequestTimeMessage(Message incomeMessage) {
//        TODO добавить кнопки с промежутками времени если выезд сегодня.
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE();
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, messageText, chatStatus);
        log.debug("method: createNewRequestTimeMessage");
        return editMessage;
    }

    private void createNewRequestSetTime(long chatId, LocalTime time) {
        log.debug("method createNewRequestSetTime");
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setDepartureTime(time);
        dtoOperations.update(chatId, dto);
    }

    private SendMessage createNewRequestChooseCarMessage(long chatId) {
//        TODO Если у пользователя один автомобиль сделать кнопку добавления автомобиля и переделать для соответствия текст
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_PASSENGER_REQUEST_CHOSE_CAR_MESSAGE() + carHandler.CarListToString(chatId));
        String callback = FindPassengerRequestOperation.CREATE_REQUEST_CAR_CALLBACK.getValue();
        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(createCarChoiceButtonsAttributesList(callback, chatId)));
        log.debug("method: createNewRequestChooseCarMessage");
        return sendMessage;
    }

    private void setDtoCar(long chatId, int carId) {
        log.debug("method createNewRequestSetCar");
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setCar(carService.findById(carId));
        dtoOperations.update(chatId, dto);
    }

    private EditMessageText createNewRequestSeatsMessage(Message incomeMessage) {
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.CREATE_REQUEST_SEATS_STATUS.getValue();
        editMessage = createSeatsMessage(incomeMessage, chatStatus);
        log.debug("method: createNewRequestSeatsMessage");
        return editMessage;
    }

    private void setDtoSeatsQuantity(long chatId, int seatsQuantity) {
        log.debug("method setDtoSeatsQuantity");
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setSeatsQuantity(seatsQuantity);
        dtoOperations.update(chatId, dto);
    }

    private SendMessage createNewRequestCommentaryMessage(long chatId) {
        sendMessage = createCommentaryMessage(chatId, handlerPrefix);
        log.debug("method: createNewRequestCommentaryMessage");
        return sendMessage;
    }

    private void setDtoCommentary(long chatId, String commentary) {
        log.debug("method createNewRequestSetCommentary");
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId).setCommentary(firstLetterToUpperCase(commentary));
        dtoOperations.update(chatId, dto);
    }

    private EditMessageText checkDataBeforeSaveMessageSkipComment(Message incomeMessage) {
        String messageText = dtoToString(dtoOperations.getDTO(incomeMessage.getChatId()));
        editMessage = createCheckDataBeforeSaveMessageSkipComment(incomeMessage, messageText, handlerPrefix);
        log.debug("method checkDataBeforeSaveMessageSkipComment");
        return editMessage;
    }

    private SendMessage checkDataBeforeSaveMessage(long chatId) {
        String messageText = dtoToString(dtoOperations.getDTO(chatId));
        sendMessage = createCheckDataBeforeSaveMessage(chatId, messageText, handlerPrefix);
        log.debug("method checkDataBeforeSaveMessage");
        return sendMessage;
    }

    private EditMessageText saveRequestSuccessMessage(Message incomeMessage, FindPassengerRequest request) {
        String messageText = requestToString(request);
        editMessage = createRequestSaveSuccessMessage(incomeMessage, messageText);
        log.debug("method saveRequestSuccessMessage");
        return editMessage;
    }

    private EditMessageText startEditBeforeSaveRequestMessage(Message incomeMessage) {
        editMessage = createStartEditRequestMessage(incomeMessage, createEditButtonsAttributesList());
        log.debug("method: startEditBeforeSaveRequestMessage");
        return editMessage;
    }

    private List<Pair<String, String>> createEditButtonsAttributesList() {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.settlementLocationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_SETTLEMENT_LOCATION_CALLBACK.getValue())); // Edit settlements or locations button
        buttonsAttributesList.add(buttons.dateTimeButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DATE_TIME_CALLBACK.getValue())); // Edit date or time button
        buttonsAttributesList.add(buttons.passengerQuantityButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CAR_DETAILS_CALLBACK.getValue())); // Change car or seats quantity button
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
        buttonsAttributesList.add(buttons.departureLocationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DEPARTURE_LOCATION_CALLBACK.getValue())); // Edit destination settlement button
        buttonsAttributesList.add(buttons.destinationSettlementButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DESTINATION_SETTLEMENT_CALLBACK.getValue())); // Edit departure location button
        buttonsAttributesList.add(buttons.destinationLocationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DESTINATION_LOCATION_CALLBACK.getValue())); // Edit destination location button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private EditMessageText editBeforeSaveDepartureSettlementMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DEPARTURE_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseOfAllSettlementsMessage(incomeMessage, messageText, callback);
        log.debug("method: EditBeforeSaveDepartureSettlementMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDepartureLocationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_REQUEST_DEPARTURE_LOCATION_MESSAGE());
        int settlementId = dtoOperations.getDTO(incomeMessage.getChatId()).getDepartureSettlement().getId();
        String callbackData = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DEPARTURE_LOCATION_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.locationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDepartureLocationMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDestinationSettlementMessage(Message incomeMessage) {
        String messageText = messages.getCREATE_REQUEST_DESTINATION_SETTLEMENT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DESTINATION_SETTLEMENT_CALLBACK.getValue();
        editMessage = createChooseOfAllSettlementsMessage(incomeMessage, messageText, callback);
        log.debug("method: EditBeforeSaveDestinationSettlementMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDestinationLocationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_REQUEST_DESTINATION_LOCATION_MESSAGE());
        int settlementId = dtoOperations.getDTO(incomeMessage.getChatId()).getDestinationSettlement().getId();
        String callbackData =
                handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_DESTINATION_LOCATION_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.locationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: EditBeforeSaveDestinationLocationMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveDateTimeMessage(Message incomeMessage) {
        String callbackDate = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_DATE_CALLBACK.getValue();
        String callbackTime = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_TIME_CALLBACK.getValue();
        editMessage = createDateTimeMessage(incomeMessage, callbackDate, callbackTime);
        log.debug("method: EditBeforeSaveDateTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveCarDetailsMessage(Message incomeMessage) {
        String carCallback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CAR_CALLBACK.getValue();
        String seatsCallback = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_SEATS_QUANTITY_CALLBACK.getValue();
        editMessage = createCarDetailsMessage(incomeMessage, carCallback, seatsCallback);
        log.debug("method: EditBeforeSaveCarDetailsMessage");
        return editMessage;
    }

    private void editBeforeSaveSwapDepartureDestination(long chatId) {
        log.debug("method: editBeforeSaveSwapDepartureDestination");
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId);
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
        dtoOperations.update(chatId, dto);
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
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE();
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_TIME_STATUS.getValue();
        editMessage = createTimeMessage(incomeMessage, messageText, chatStatus);
        log.debug("method: editBeforeSaveTimeMessage");
        return editMessage;
    }

    private EditMessageText editBeforeSaveChooseCarMessage(Message incomeMessage) {
        String callback = FindPassengerRequestOperation.EDIT_BEFORE_SAVE_CHANGE_CAR_CALLBACK.getValue();
        editMessage = createChooseCarMessage(incomeMessage, callback);
        log.debug("method: editBeforeSaveChooseCarMessage");
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

    private FindPassengerRequest saveRequest(long chatId) {
        FindPassengerRequestDTO dto = dtoOperations.getDTO(chatId);
        dtoOperations.delete(chatId);
        chatStatusOperations.deleteChatStatus(chatId);
        log.debug("method saveRequest");
        return findPassengerRequestService.addNewRequest(dto);
    }

    private EditMessageText chooseRequestToEditMessage(Message incomeMessage) {
//        TODO рефакторинг - вынести метод в суперкласс
        String message = messages.getCHOOSE_REQUEST_TO_EDIT_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.EDIT_REQUEST_START_CALLBACK.getValue();
        editMessage = createChoiceRequestMessage(incomeMessage, message, callback);
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
        buttonsAttributesList.add(buttons.carDetailsButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_CAR_DETAILS_CALLBACK.getValue() + requestId)); // Change car or seats quantity button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_COMMENTARY_CALLBACK.getValue() + requestId)); // Edit commentary button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private EditMessageText editSettlementLocationMessage(Message incomeMessage, int requestId) {
        List<Pair<String, String>> buttonsAttributesList = createEditSettlementsLocationButtonsAttributesList(handlerPrefix, requestId);
        editMessage = createEditSettlementLocationMessage(incomeMessage, buttonsAttributesList);
        log.debug("method: editSettlementLocationMessage");
        return editMessage;
    }

    private List<Pair<String, String>> createEditSettlementsLocationButtonsAttributesList(String handlerPrefix, int requestId) {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.swapDepartureDestinationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_SWAP_DEPARTURE_DESTINATION_CALLBACK.getValue() + requestId)); // Swap departure and destination button
        buttonsAttributesList.add(buttons.departureSettlementButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_DEPARTURE_SETTLEMENT_CALLBACK.getValue() + requestId)); // Edit departure settlement button
        buttonsAttributesList.add(buttons.departureLocationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_DEPARTURE_LOCATION_CALLBACK.getValue() + requestId)); // Edit destination settlement button
        buttonsAttributesList.add(buttons.destinationSettlementButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_DESTINATION_SETTLEMENT_CALLBACK.getValue() + requestId)); // Edit departure location button
        buttonsAttributesList.add(buttons.destinationLocationButtonCreate(handlerPrefix
                + FindPassengerRequestOperation.EDIT_DESTINATION_LOCATION_CALLBACK.getValue() + requestId)); // Edit destination location button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
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

    private EditMessageText editDepartureLocationMessage(Message incomeMessage, int requestId, int settlementId) {
        log.debug("method: editDepartureLocationMessage");
        String messageText = messages.getCREATE_REQUEST_DEPARTURE_LOCATION_MESSAGE();
        String callback = handlerPrefix + String.format(FindPassengerRequestOperation.EDIT_CHANGE_DEPARTURE_LOCATION_CALLBACK.getValue(), requestId);
        return editLocationMessage(incomeMessage, messageText, callback, settlementId);
    }

    private EditMessageText editDestinationLocationMessage(Message incomeMessage, int requestId, int settlementId) {
        log.debug("method: editDestinationLocationMessage");
        String messageText = messages.getCREATE_REQUEST_DESTINATION_LOCATION_MESSAGE();
        String callback = handlerPrefix + String.format(FindPassengerRequestOperation.EDIT_CHANGE_DESTINATION_LOCATION_CALLBACK.getValue(), requestId);
        return editLocationMessage(incomeMessage, messageText, callback, settlementId);
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
        String messageText = messages.getCREATE_FIND_PASSENGER_REQUEST_TIME_MESSAGE();
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

    private FindPassengerRequest editSetTime(int requestId, LocalTime time) {
        log.debug("method editSetTime");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        LocalDateTime rideDate = request.getDepartureAt();
        rideDate = rideDate.withHour(time.getHour()).withMinute(time.getMinute());
        request.setDepartureAt(rideDate);
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editCarDetailsMessage(Message incomeMessage, int requestId) {
        String carCallback = handlerPrefix + FindPassengerRequestOperation.EDIT_CAR_CALLBACK.getValue() + requestId;
        String seatsCallback = handlerPrefix + FindPassengerRequestOperation.EDIT_SEATS_QUANTITY_CALLBACK.getValue() + requestId;
        editMessage = createCarDetailsMessage(incomeMessage, carCallback, seatsCallback);
        log.debug("method: editCarDetailsMessage");
        return editMessage;
    }

    private EditMessageText editChooseCarMessage(Message incomeMessage, int requestId) {
        String callback = String.format(FindPassengerRequestOperation.EDIT_CHANGE_CAR_CALLBACK.getValue(), requestId);
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
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_SEATS_QUANTITY_STATUS.getValue() + requestId;
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
        String chatStatus = handlerPrefix + FindPassengerRequestOperation.EDIT_CHANGE_COMMENTARY_STATUS.getValue() + requestId;
        createCommentaryMessage(incomeMessage, chatStatus);
        log.debug("method: editBeforeSaveCommentaryMessage");
        return editMessage;
    }

    private FindPassengerRequest setEditedCommentary(int requestId, String commentary) {
        log.debug("method: setEditedSeatsQuantity");
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        request.setCommentary(firstLetterToUpperCase(commentary));
        return findPassengerRequestService.updateRequest(request);
    }

    private EditMessageText editRequestSuccessEditMessage(Message incomeMessage, FindPassengerRequest request) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_SUCCESS_EDITION_MESSAGE() + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusOperations.deleteChatStatus(incomeMessage.getChatId());
        return editMessage;
    }

    private SendMessage editRequestSuccessSendMessage(long chatId, FindPassengerRequest request) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getFIND_PASSENGER_SUCCESS_EDITION_MESSAGE() + requestToString(request) + messages.getFURTHER_ACTION_MESSAGE());
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusOperations.deleteChatStatus(chatId);
        return sendMessage;
    }

    private EditMessageText chooseRequestToCancelMessage(Message incomeMessage) {
        String message = messages.getCHOOSE_REQUEST_TO_CANCEL_MESSAGE();
        String callback = handlerPrefix + FindPassengerRequestOperation.CANCEL_REQUEST_CALLBACK.getValue();
        editMessage = createChoiceRequestMessage(incomeMessage, message, callback);
        log.debug("method: chooseRequestToEditMessage");
        return editMessage;
    }

    private FindPassengerRequest onCancelingRequestReceived(int requestId) {
        log.debug("method onCancelingRequestReceived");
        FindPassengerRequest request = findPassengerRequestService.cancelRequestById(requestId);

//    TODO Удаление брони.
        bookingService.removeBookingByCancelingRequest(RequestsType.FIND_PASSENGER_REQUEST, requestId);
        findPassengerRequestService.removeFromRedis(requestId);
////     Сообщение об удалении брони обеим сторонам и новые варианты для второй стороны
//        }
//
//        if (rideService.hasRide(RequestsType.FIND_PASSENGER_REQUEST, requestId)) {
//            //    TODO Удаление поездки.
//            //     Сообщение об удалении брони обеим сторонам и новые варианты для второй стороны
//        }

        return request;
    }

    private EditMessageText sendCancelRequestSuccessMessage(Message incomeMessage, FindPassengerRequest request) {
        String requestToString = requestToString(request);
        editMessage = createCancelRequestSuccessMessage(incomeMessage, requestToString);
        log.debug("method: cancelRequestSuccessMessage");
        return editMessage;
    }

    private EditMessageText createChoiceRequestMessage(Message incomeMessage, String messageText, String callback) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messageText + requestListToString(incomeMessage.getChatId()));
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(requestButtonsAttributesListCreator(callback, incomeMessage.getChatId())));
        log.debug("method: createChoiceRequestMessage");
        return editMessage;
    }

    private EditMessageText sendNecessityToCancelMessage(Message incomeMessage) {
        editMessage = createNecessityToCancelMessage(incomeMessage, handlerPrefix);
        log.debug("method: createNecessityToCancelMessage");
        return editMessage;
    }

    public void sendExpireDepartureTimeMessage(int requestId) {
        FindPassengerRequest request = findPassengerRequestService.findById(requestId);
        sendMessage = createExpireRequestTimeMessage(request.getUser().getChatId(), requestToString(request));
        log.debug("method: sendExpireDepartureTimeMessage");
        sendBotMessage(sendMessage);
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

    private List<Pair<String, String>> createCarChoiceButtonsAttributesList(String callback, long chatId) {
        List<Pair<String, String>> buttonsAttributesList =
                carHandler.carButtonsAttributesListCreator(handlerPrefix + callback, chatId);
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        return buttonsAttributesList;
    }

    private FindPassengerRequest getLastRequest(long chatId) {
        return findPassengerRequestService.findLastUserRequest(chatId);
    }

    private Optional<FindPassengerRequest> getLastRequestOptional(long chatId) {
        return findPassengerRequestService.findLastUserRequestOptional(chatId);
    }

    private List<FindPassengerRequest> getUserActiveFindPassengerRequestsList(long chatId) {
        return findPassengerRequestService.usersActiveRequestList(chatId);
    }

    private List<Pair<String, String>> requestButtonsAttributesListCreator(String callbackData, long chatId) {
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

    private boolean isRequestQuantityLimit(long chatId) {
        int maxRequestsQuantity = 3;
        return getUserActiveFindPassengerRequestsList(chatId).size() >= maxRequestsQuantity;
    }
    // return true if User send message with the time in correct regex, it correctly parsed to the time and this time still not expired
    private boolean isACorrectTimeMessageOrTime(LocalTime time, Long chatId) {
        return time.toNanoOfDay() == 100 || isExpired(chatId, time);
    }

    private boolean isExpired(long chatId, LocalTime time) {
        return dtoOperations.getDTO(chatId).getDepartureDate().isEqual(LocalDate.now()) && time.isBefore(LocalTime.now());
    }

    private boolean isExpired(long chatId) {
        return dtoOperations.getDTO(chatId).getDepartureTime().isBefore(LocalTime.now());
    }

    private boolean isExpired(int requestId) {
        return findPassengerRequestService.findById(requestId).getDepartureAt().isBefore(LocalDateTime.now());
    }

    private boolean isExpired(int requestId, LocalTime time) {
        return findPassengerRequestService.findById(requestId).getDepartureAt().toLocalDate().isEqual(LocalDate.now()) && time.isBefore(LocalTime.now());
    }
}
