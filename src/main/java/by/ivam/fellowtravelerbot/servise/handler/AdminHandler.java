package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
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
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;


/*
This class handle Admin functional
 */
@Service
@Data
@Log4j
public class AdminHandler {
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
    public SendMessage settlementNameRequestMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADD_SETTLEMENT_NAME_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        chatStatusStorageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.ADD_SETTLEMENT_NAME));
        log.debug("AdminHandler method settlementNameRequestMessage");
        return sendMessage;
    }

    public Settlement saveSettlement(Long chatId, String settlementName) {
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method saveSettlement: call to save " + settlementName + " to DB");
        return settlementService.addNewSettlement(CommonMethods.firstLetterToUpperCase(settlementName));
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
//        location.setSettlement(settlementService.findById(Integer.parseInt(callbackData.substring(36))));
        location.setSettlement(settlementService.findById(CommonMethods.trimId2(callbackData)));
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

    private String firstLetterToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
