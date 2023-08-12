package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.PickUpPassengerRequestDTO;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.model.Direction;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.servise.CarService;
import by.ivam.fellowtravelerbot.servise.SettlementService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import by.ivam.fellowtravelerbot.storages.interfaces.PickUpPassengerStorageAccess;
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
public class PickUpPassengerHandler implements Handler{
    @Autowired
    Messages messages;
    @Autowired
    Keyboards keyboards;
    @Autowired
    Buttons buttons;
    @Autowired
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    UserService userService;
    @Autowired
    CarService carService;
    @Autowired
    PickUpPassengerStorageAccess pickUpPassengerStorageAccess;
    @Autowired
    SettlementService settlementService;
    @Autowired
    ResponseMessageProcessor messageProcessor;
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        log.debug("method handleReceivedMessage");
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug("method handleReceivedCallback. get callback: " + callback);
    }

    public void startCreateNewRequest(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_START_PROCESS_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getYES_BUTTON_TEXT(),
                buttons.getCREATE_PICKUP_PASSENGER_REQUEST_CALLBACK())); //yes button
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getCANCEL_BUTTON_TEXT(),
                buttons.getCANCEL_CALLBACK())); //cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: startCreatePickUpPassengerRequestProcess");
        messageProcessor.sendMessage(sendMessage);
    }

    public void createPickUpPassengerRequestDTO(long chatId) {
        PickUpPassengerRequestDTO pickUpPassengerRequestDTO = new PickUpPassengerRequestDTO();
        pickUpPassengerRequestDTO.setUser(userService.findUserById(chatId));
        pickUpPassengerStorageAccess.addPickUpPassengerDTO(chatId, pickUpPassengerRequestDTO);
        log.debug("method: createPickUpPassengerRequestDTO - create DTO " + pickUpPassengerRequestDTO + " and save it in storage");
    }

    public EditMessageText createNewRequestChoseDirectionMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_DIRECTION_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getTOWARD_MINSK_TEXT(),
                buttons.getCREATE_PICKUP_PASSENGER_REQUEST_DIRECTION_CALLBACK() + Direction.TOWARDS_MINSK)); //toward Minsk button
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getFROM_MINSK_TEXT(),
                buttons.getCREATE_PICKUP_PASSENGER_REQUEST_DIRECTION_CALLBACK() + Direction.FROM_MINSK)); //from Minsk button
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getCANCEL_BUTTON_TEXT(),
                buttons.getCANCEL_CALLBACK())); //cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createPickUpPassengerRequestProcessChoseDirectionMessage");

        return editMessage;
    }


    public void createNewRequestSetDirection(long chatId, Direction direction) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");
        pickUpPassengerStorageAccess.setDirection(chatId, direction);
    }

    public EditMessageText createNewRequestChooseResidenceToMinskMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_MESSAGE());
        Settlement settlement = userService.findUserById(incomeMessage.getChatId()).getResidence();

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(settlement.getName(),
                buttons.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_CALLBACK() + settlement.getId())); //Chose residence button
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getANOTHER_TEXT(),
                buttons.getCREATE_PICKUP_PASSENGER_REQUEST_ANOTHER_SETTLEMENT_CALLBACK())); //Chose another settlement button
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getCANCEL_BUTTON_TEXT(),
                buttons.getCANCEL_CALLBACK())); //cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createNewRequestChooseResidenceToMinskMessage");

        return editMessage;
    }

    public EditMessageText createNewRequestChooseAnotherSettlementToMinskMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_MESSAGE());
        String callbackData = buttons.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_CALLBACK();

        String residenceName = userService.findUserById(incomeMessage.getChatId()).getResidence().getName();
        List<Settlement> settlementList = settlementService.findAllExcept(residenceName, "Минск");

        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(keyboards.settlementsButtonsAttributesListCreator(settlementList,callbackData)));
        log.debug("method: createNewRequestChooseResidenceToMinskMessage");

        return editMessage;
    }

    public void createNewRequestSetSettlement(long chatId, Settlement settlement) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");
        pickUpPassengerStorageAccess.setSettlement(chatId, settlement);

    }

    public void createNewRequestSetSettlement(long chatId, String settlementName) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");

        pickUpPassengerStorageAccess.setSettlement(chatId, settlementService.findByName(settlementName));
    }
    public EditMessageText createNewRequestChooseDepartureLocationMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getCREATE_PICKUP_PASSENGER_REQUEST_DEPARTURE_LOCATION_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
//        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(settlement.getName(),
//                buttons.getCREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_CALLBACK() + settlement.getId())); //Chose residence button
//        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getANOTHER_TEXT(),
//                buttons.getCREATE_PICKUP_PASSENGER_REQUEST_ANOTHER_SETTLEMENT_CALLBACK())); //Chose another settlement button
        buttonsAttributesList.add(keyboards.buttonAttributesPairCreator(buttons.getCANCEL_BUTTON_TEXT(),
                buttons.getCANCEL_CALLBACK())); //cancel button
        editMessage.setReplyMarkup(keyboards.twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(buttonsAttributesList));
        log.debug("method: createPickUpPassengerRequestProcessChoseDirectionMessage");

        return editMessage;
    }
    public void createPickUpPassengerRequestProcessSetDepartureLocation(long chatId, Settlement settlement) {
        log.debug("method createPickUpPassengerRequestProcessSetDirection");
    }
public Settlement getSettlementFromCallback(String callbackData){
    return settlementService.findById(Integer.parseInt(callbackData.substring(51)));
}

    private void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
//        return editMessage;
    }
}
