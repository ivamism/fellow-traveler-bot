package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.servise.SettlementService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.servise.handler.enums.ChatStatus;
import by.ivam.fellowtravelerbot.storages.StorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


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
    Messages messages;
    @Autowired
    Keyboards keyboards;
    @Autowired
    Buttons buttons;
    @Autowired
    StorageAccess storageAccess;

//    TODO Добавить логирование

    SendMessage sendMessage = new SendMessage();
    public boolean checkIsAdmin (long chatId){
log.debug("AdminHandler method checkIsAdmin");
        return userService.findUserById(chatId).isAdmin();
    }

    public SendMessage showAdminMenuMessage(long chatId){
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADMIN_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.mainAdminMenu());
        log.debug("AdminHandler method checkIsAdmin");

        return sendMessage;
    }
    public SendMessage settlementNameRequestMessage(long chatId){
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getADD_SETTLEMENT_NAME_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(chatId, String.valueOf(ChatStatus.ADD_SETTLEMENT_NAME));
        log.debug("AdminHandler method settlementNameRequestMessage");
        return sendMessage;
    }
    public Settlement saveSettlement(Long chatId, String settlementName) {
        storageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method saveSettlement: call to save " + settlementName + " to DB");
        return settlementService.addNewSettlement(firstLetterToUpperCase(settlementName));
    }
    public SendMessage settlementSaveSuccessMessage(long chatId, Settlement settlement){
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getADD_SETTLEMENT_SUCCESS_MESSAGE(), settlement.getId(),settlement.getName()));
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("AdminHandler method settlementSaveSuccessMessage");
        return sendMessage;
    }

    private String firstLetterToUpperCase(String s) {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
