package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.LocationDTO;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.enums.AdminOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.model.Location;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.servise.DepartureLocationService;
import by.ivam.fellowtravelerbot.servise.SettlementService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import by.ivam.fellowtravelerbot.storages.interfaces.DepartureLocationStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/*
This class handle Admin functional
 */
@Service
@Data
@Log4j
public class AdminHandler extends Hndlr implements HandlerInterface {
    @Autowired
    UserService userService;
    @Autowired
    SettlementService settlementService;
    @Autowired
    DepartureLocationService locationService;
//    @Autowired
//    Messages messages;
//    @Autowired
//    Keyboards keyboards;
//    @Autowired
//    Buttons buttons;
//    @Autowired
//    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    DepartureLocationStorageAccess departureLocationStorageAccess;
//    @Autowired
//    ResponseMessageProcessor messageProcessor;

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
            case "ADD_SETTLEMENT_NAME_CHAT_STATUS" -> {
                Settlement settlement = saveSettlement(chatId, messageText);
                sendMessage = settlementSaveSuccessMessage(chatId, settlement);
            }case "DEPARTURE_LOCATION_REQUEST_NAME_CHAT_STATUS" -> {
                Location location = departureLocationSave(chatId, messageText);
                sendMessage = departureLocationSaveSuccessMessage(chatId, location);
            }

        }
        sendBotMessage(sendMessage);
//        messageProcessor.sendMessage(sendMessage);
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug("method handleReceivedCallback. get callback: " + callback);
        Long chatId = incomeMessage.getChatId();
        String process = callback;
        if (callback.contains(":")) {
            process = trimProcess(callback);
        }
        log.debug("process: " + process);
        switch (process){
            case "DEPARTURE_LOCATION_SET_SETTLEMENT_CALLBACK" -> {
                departureLocationSetSettlement(chatId, callback);
                editMessage = departureLocationNameRequestMessage(incomeMessage);
            }
        }
        sendEditMessage(editMessage);
//        messageProcessor.sendEditedMessage(editMessage);
    }


    public void handleReceivedCommand(String command, Message incomemessage) {
        long chatId = incomemessage.getChatId();
        switch (command) {
            case "Добавить нас. пункт" -> sendMessage = settlementNameRequestMessage(chatId);
            case "Добавить локацию" -> sendMessage = departureLocationSettlementRequestMessage(chatId);

        }
        sendBotMessage(sendMessage);
//        messageProcessor.sendMessage(sendMessage);
    }

    public boolean checkIsAdmin(long chatId) {
        log.debug("AdminHandler method checkIsAdmin");
        return userService.findUserById(chatId).isAdmin();
    }

    public void showAdminMenuMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADMIN_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.mainAdminMenu());
        log.debug("AdminHandler method checkIsAdmin");
//        messageProcessor.sendMessage(sendMessage);
        sendBotMessage(sendMessage);
    }

    // Handle Settlement
    private SendMessage settlementNameRequestMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADD_SETTLEMENT_NAME_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(chatId, Handlers.ADMIN.getHandlerPrefix() + AdminOperation.ADD_SETTLEMENT_NAME_CHAT_STATUS);
        log.debug("AdminHandler method settlementNameRequestMessage");
        return sendMessage;
    }

    private Settlement saveSettlement(Long chatId, String settlementName) {
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method saveSettlement: call to save " + settlementName + " to DB");
        return settlementService.addNewSettlement(firstLetterToUpperCase(settlementName));
    }

    private SendMessage settlementSaveSuccessMessage(long chatId, Settlement settlement) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getADD_SETTLEMENT_SUCCESS_MESSAGE(), settlement.getId(), settlement.getName()));
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("AdminHandler method settlementSaveSuccessMessage");
        return sendMessage;
    }

    // Handle Location
    private SendMessage departureLocationSettlementRequestMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList =
                settlementsButtonsAttributesListCreator(Handlers.ADMIN.getHandlerPrefix() + AdminOperation.DEPARTURE_LOCATION_SET_SETTLEMENT_CALLBACK.getValue()); // List of buttons of Settlements
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        log.debug("AdminHandler method departureLocationSettlementRequestMessage");
        return sendMessage;
    }

    private void departureLocationSetSettlement(long chatId, String callbackData) {
        log.debug("AdminHandler method departureLocationSetSettlement");
        LocationDTO location = new LocationDTO();
        location.setSettlement(settlementService.findById(trimId(callbackData)));
        departureLocationStorageAccess.addLocation(chatId, location);
    }

    private EditMessageText departureLocationNameRequestMessage(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_LOCATION_NAME_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.addChatStatus(chatId, Handlers.ADMIN.getHandlerPrefix() + AdminOperation.DEPARTURE_LOCATION_REQUEST_NAME_CHAT_STATUS);

        log.debug("AdminHandler method departureLocationNameRequestMessage");
        return editMessage;
    }

    private Location departureLocationSave(long chatId, String name) {
        log.debug("AdminHandler method departureLocationSave");
        LocationDTO locationDTO = departureLocationStorageAccess.findDTO(chatId);
        locationDTO.setName(firstLetterToUpperCase(name));
        departureLocationStorageAccess.deleteLocation(chatId);
        chatStatusStorageAccess.deleteChatStatus(chatId);

        return locationService.addNewLocation(locationDTO);

    }

    private SendMessage departureLocationSaveSuccessMessage(long chatId, Location location) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getADD_LOCATION_SUCCESS_MESSAGE(), location.getId(), location.getName(), location.getSettlement().getName()));
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("AdminHandler method settlementSaveSuccessMessage");
        return sendMessage;
    }

    private List<Settlement> getSettlementsList() {
        return settlementService.findAll();
    }
    private List<Location> getDepartureLocationListBySettlement(int settlementId) {
        return  locationService.findAllBySettlement(settlementId);
    }

    public List<Pair<String, String>> settlementsButtonsAttributesListCreator(String callbackData) {
        Map<Integer, String> settlementAttributes = getSettlementsList()
                .stream()
                .collect(Collectors.toMap(settlement -> settlement.getId(), settlement -> settlement.getName()));
        return keyboards.buttonsAttributesListCreator(settlementAttributes, callbackData);
    }
    public List<Pair<String, String>> departureLocationButtonsAttributesListCreator(String callbackData, int settlementId) {
        Map<Integer, String> locationAttributes = getDepartureLocationListBySettlement(settlementId)
                .stream()
                .collect(Collectors.toMap(location -> location.getId(), location -> location.getName()));
        return keyboards.buttonsAttributesListCreator(locationAttributes, callbackData);
    }
}
