package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
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
            case "CREATE_FIND_PAS_REQUEST_DIRECTION" -> {
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
                createNewRequestProcessSetDepartureLocation(chatId, trimId(callback));
//                editMessage = createNewRequestChooseAnotherSettlementAsDepartureMessage(incomeMessage);
            }

        }
        sendEditMessage(editMessage);
    }

    public void startCreateNewRequest(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_START_PROCESS_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_FIND_PASSENGER_REQUEST_CALLBACK)); // Add car button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        log.debug("method: startCreatePickUpPassengerRequestProcess");
        sendBotMessage(sendMessage);
    }

    public void createFindPassengerRequestDTO(long chatId) {
        FindPassengerRequestDTO findPassengerRequestDTO = new FindPassengerRequestDTO();
        findPassengerRequestDTO.setUser(userService.findUserById(chatId));
        findPassengerStorageAccess.addPickUpPassengerDTO(chatId, findPassengerRequestDTO);
        log.debug("method: createPickUpPassengerRequestDTO - create DTO " + findPassengerRequestDTO + " and save it in storage");
    }

    public EditMessageText createNewRequestChoseDirectionMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_DIRECTION_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)

        buttonsAttributesList.add(buttons.towardMinskButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_FIND_PASSENGER_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.TOWARDS_MINSK)); // toward Minsk button
        buttonsAttributesList.add(buttons.fromMinskButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_FIND_PASSENGER_REQUEST_DIRECTION_CALLBACK.getValue() + Direction.FROM_MINSK)); // from Minsk button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));

        log.debug("method: createPickUpPassengerRequestProcessChoseDirectionMessage");

        return editMessage;
    }

    public void createNewRequestSetDirection(long chatId, String direction) {
        log.debug("method createNewRequestSetDirection");
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDirection(direction);
        findPassengerStorageAccess.update(chatId, dto);
    }

    public EditMessageText createNewRequestChooseResidenceAsDepartureMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_MESSAGE());
        Settlement settlement = userService.findUserById(incomeMessage.getChatId()).getResidence();

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.buttonCreate(settlement.getName(), Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_FIND_PASSENGER_REQUEST_SETTLEMENT_CALLBACK.getValue() + settlement.getId()));
        buttonsAttributesList.add(buttons.anotherButtonCreate(Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_FIND_PASSENGER_REQUEST_ANOTHER_SETTLEMENT_CALLBACK.getValue()));
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button

        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChooseResidenceAsDepartureMessage");

        return editMessage;
    }

    public EditMessageText createNewRequestChooseAnotherSettlementAsDepartureMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_FIND_PASSENGER_REQUEST_SETTLEMENT_CALLBACK.getValue();
//        String callbackData = buttons.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_CALLBACK();

        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.settlementsButtonsAttributesListCreator(callbackData, settlementList);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(keyboards.settlementsButtonsAttributesListCreator(settlementList, callbackData)));
        log.debug("method: createNewRequestChooseResidenceToMinskMessage");

        return editMessage;
    }
    public void createNewRequestSetDepartureSettlement(long chatId, int settlementId) {
        log.debug("method createNewRequestSetDepartureSettlement");
        Settlement settlement = settlementService.findById(settlementId);
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDepartureSettlement(settlement);
        findPassengerStorageAccess.update(chatId, dto);
//        findPassengerStorageAccess.setDepartureSettlement(chatId, settlementService.findByName(settlementName));
    }
    public EditMessageText createNewRequestChooseDepartureLocationMessage(Message incomeMessage, int settlementId) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_DEPARTURE_LOCATION_MESSAGE());
        String callbackData = Handlers.FIND_PASSENGER.getHandlerPrefix() + FindPassengerOperation.CREATE_REQUEST_DEP_LOCATION_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList =
                adminHandler.departureLocationButtonsAttributesListCreator(callbackData, settlementId);
        buttonsAttributesList.add(buttons.cancelButtonCreate());
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createPickUpPassengerRequestProcessChoseDirectionMessage");

        return editMessage;
    }

    public void createNewRequestProcessSetDepartureLocation(long chatId, int locationId) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");
        Location location = locationService.findById(locationId);
        FindPassengerRequestDTO dto = findPassengerStorageAccess.getDTO(chatId).setDepartureLocation(location);
        findPassengerStorageAccess.update(chatId, dto);
    }

    private void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }
}
