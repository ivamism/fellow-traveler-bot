package by.ivam.fellowtravelerbot.bot.dispatcher;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Data
@Log4j
public class CallbackDispatcher extends Dispatcher {

    public void onCallbackReceived(CallbackQuery callbackQuery) {
        Message incomeMessage = callbackQuery.getMessage();
        String callbackData = callbackQuery.getData();
        log.info("Get callbackData: " + callbackData);
        String handler = getHandler(callbackData);
        String callback = getProcess(callbackData);
        log.info("Get callBack: " + callbackData);
        log.info("Handler: " + handler);
        switch (handler) {
            case "START" -> startHandler.handleReceivedCallback(callback, incomeMessage);
            case "ADMIN" -> adminHandler.handleReceivedCallback(callback, incomeMessage);
            case "USER" -> userHandler.handleReceivedCallback(callback, incomeMessage);
            case "CAR" -> carHandler.handleReceivedCallback(callback, incomeMessage);
            case "FIND_RIDE" -> findRideHandler.handleReceivedCallback(callback, incomeMessage);
            case "FIND_PAS" -> findPassengerHandler.handleReceivedCallback(callback, incomeMessage);
        }
    }
}
