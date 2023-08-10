package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.servise.handler.AdminHandler;
import by.ivam.fellowtravelerbot.servise.handler.CarHandler;
import by.ivam.fellowtravelerbot.servise.handler.StartHandler;
import by.ivam.fellowtravelerbot.servise.handler.UserHandler;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@Data
@Log4j
public class CallbackDispatcher {
    @Autowired
    StartHandler startHandler;
    @Autowired
    AdminHandler adminHandler;
    @Autowired
    UserHandler userHandler;
    @Autowired
    CarHandler carHandler;

    public void onCallbackReceived(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        String handler = getHandler(callbackData);
        String callback = getCallback(callbackData);
        log.info("Get callBack: " + callbackData);
        log.info("Handler: " + handler);
        switch (getHandler(callbackData)) {
            case "START" -> {

            }
            case "ADMIN" -> {

            }
            case "USER" -> {
                userHandler.handleReceivedCallback(callback);
            }
            case "CAR" -> {

            }
            case "FIND_RIDE" -> {

            }

            case "PICKUP_PASSENGER" -> {

            }
        }


    }

    private String getHandler(String callbackData) {
        String[] strings = callbackData.split("-");
        return strings[0];
    }

    private String getCallback(String callbackData) {
        String[] strings = callbackData.split("-");
        return strings[1];
    }


//
//            Message incomeMessage = update.getCallbackQuery().getMessage();
//            int messageId = incomeMessage.getMessageId();
//            long chatId = incomeMessage.getChatId();
//            String messageText = incomeMessage.getText();
//            String userName = incomeMessage.getChat().getFirstName();
//            log.info("get callback: " + callbackData);
}
