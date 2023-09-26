package by.ivam.fellowtravelerbot.servise.handler;


import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.bot.enums.Direction;
import by.ivam.fellowtravelerbot.bot.enums.requestOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
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

import java.util.ArrayList;
import java.util.List;


// This class handle operations with search of hitchhiker's rides
@Service
@Data
@Log4j
public class FindRideHandler extends Handler implements HandlerInterface {
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
                if (isRequestQuantityLimit(chatId)) editMessage = createNecessityToCancelMessage(incomeMessage);
                else {
                    createRequestDTO(chatId);
                    editMessage = createNewRequestChoseDirectionMessage(incomeMessage);
                }
            }
            case "CREATE_REQUEST_DIRECTION" -> {
                String direction = trimSecondSubstring(callback);
                setDTODirection(chatId, direction);
                if (direction.equals(String.valueOf(Direction.FROM_MINSK))) {
                    int settlementId = settlementService.findByName("Минск").getId();
                    setDTODepartureSettlement(chatId, settlementId);
                    editMessage = nextStep(incomeMessage);
//                    createNewRequestChooseDepartureLocationMessage(incomeMessage, settlementId);
                } else if ((direction.equals(String.valueOf(Direction.TOWARDS_MINSK)))) {
                    editMessage = nextStep(incomeMessage);
//                    createNewRequestChooseResidenceAsDepartureMessage(incomeMessage);
                }
            }
        }
        sendEditMessage(editMessage);
    }

    public void startCreateNewRequest(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_FIND_RIDE_REQUEST_START_PROCESS_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(handlerPrefix + requestOperation.CREATE_REQUEST_CALLBACK.getValue())); // Create new request button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
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

    private void setDTODirection(long chatId, String direction) {
        log.debug("method createNewRequestSetDirection");
        FindRideRequestDTO dto = storageAccess.getDTO(chatId).setDirection(direction);
        storageAccess.update(chatId, dto);
    }

    private void setDTODepartureSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDepartureSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindRideRequestDTO dto = storageAccess.getDTO(chatId).setDepartureSettlement(settlement);
        storageAccess.update(chatId, dto);
    }



    private EditMessageText createNecessityToCancelMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getFIND_PASSENGER_NECESSITY_TO_CANCEL_REQUEST_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(handlerPrefix + requestOperation.CHOOSE_REQUEST_TO_CANCEL_CALLBACK.getValue())); // Edit date button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNecessityToCancelMessage");
        return editMessage;
    }

    private boolean isRequestQuantityLimit(long chatId) {
        return getUserActiveRequestsList(chatId).size() > 2;
    }

    private List<FindRideRequest> getUserActiveRequestsList(long chatId) {
        return findRideRequestService.usersActiveRequestList(chatId);
    }

    public void editMessageTextGeneralPreset(Message incomeMessage) {
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

