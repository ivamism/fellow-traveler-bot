package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.enums.AdminOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.servise.DepartureLocationService;
import by.ivam.fellowtravelerbot.servise.SettlementService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.bot.enums.ChatStatus;
import by.ivam.fellowtravelerbot.storages.interfaces.DepartureLocationStorageAccess;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
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
public class AdminHandler implements Handler {
    @Autowired
    UserService userService;
    @Autowired
    SettlementService settlementService;
    @Autowired
    DepartureLocationService locationService;
    @Autowired
    Messages messages;
    @Autowired
    Keyboards keyboards;
    @Autowired
    Buttons buttons;
    @Autowired
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    DepartureLocationStorageAccess departureLocationStorageAccess;
    @Autowired
    ResponseMessageProcessor messageProcessor;

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
            }
        }
        messageProcessor.sendMessage(sendMessage);
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
//        switch (process){
//
//        }
    }

    @Override
    public void handleReceivedCommand(String command, Message incomemessage) {
        long chatId = incomemessage.getChatId();
        switch (command) {
            case "Добавить нас. пункт" -> {
                sendMessage = settlementNameRequestMessage(chatId);
            }
            case "Добавить локацию" -> {
                sendMessage = departureLocationSettlementRequestMessage(chatId);
            }

        }
        messageProcessor.sendMessage(sendMessage);
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
        messageProcessor.sendMessage(sendMessage);
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

    public Settlement saveSettlement(Long chatId, String settlementName) {
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method saveSettlement: call to save " + settlementName + " to DB");
        return settlementService.addNewSettlement(firstLetterToUpperCase(settlementName));
    }

    public SendMessage settlementSaveSuccessMessage(long chatId, Settlement settlement) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getADD_SETTLEMENT_SUCCESS_MESSAGE(), settlement.getId(), settlement.getName()));
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("AdminHandler method settlementSaveSuccessMessage");
        return sendMessage;
    }

    // Handle DepartureLocation
    public SendMessage departureLocationSettlementRequestMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE());

        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(keyboards.settlementsButtonsAttributesListCreator(getSettlementsList(), buttons.getADD_LOCATION_GET_SETTLEMENT_CALLBACK())));
        log.debug("AdminHandler method departureLocationSettlementRequestMessage");
        return sendMessage;
    }

    public void departureLocationSetSettlement(long chatId, String callbackData) {
        log.debug("AdminHandler method departureLocationSetSettlement");
        DepartureLocationDTO location = new DepartureLocationDTO();
        location.setSettlement(settlementService.findById(trimId(callbackData)));
        departureLocationStorageAccess.addLocation(chatId, location);
    }

    public EditMessageText departureLocationNameRequestMessage(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_LOCATION_NAME_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.ADD_DEPARTURE_LOCATION_NAME));

        log.debug("AdminHandler method departureLocationNameRequestMessage");
        return editMessage;
    }

    public DepartureLocation departureLocationSave(long chatId, String name) {
        log.debug("AdminHandler method departureLocationSave");
        DepartureLocationDTO locationDTO = departureLocationStorageAccess.findDTO(chatId);
        locationDTO.setName(CommonMethods.firstLetterToUpperCase(name));
        departureLocationStorageAccess.deleteLocation(chatId);
        chatStatusStorageAccess.deleteChatStatus(chatId);

        return locationService.addNewLocation(locationDTO);

    }

    public SendMessage departureLocationSaveSuccessMessage(long chatId, DepartureLocation location) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getADD_LOCATION_SUCCESS_MESSAGE(), location.getId(), location.getName(), location.getSettlement().getName()));
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("AdminHandler method settlementSaveSuccessMessage");
        return sendMessage;
    }

    public List<Settlement> getSettlementsList() {
        return settlementService.findAll();
    }

    public List<Pair<String, String>> settlementsButtonsAttributesListCreator(String callbackData) {
        Map<Integer, String> settlementAttributes = getSettlementsList()
                .stream()
                .collect(Collectors.toMap(settlement -> settlement.getId(), settlement -> settlement.getName()));
        return keyboards.buttonsAttributesListCreator(settlementAttributes, callbackData);
    }

    private String first_LetterToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
