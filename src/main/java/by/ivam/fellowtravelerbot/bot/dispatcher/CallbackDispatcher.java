package by.ivam.fellowtravelerbot.bot.dispatcher;

import by.ivam.fellowtravelerbot.servise.FindPassengerRequestService;
import by.ivam.fellowtravelerbot.servise.FindRideRequestService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;

@Component
@Data
@Log4j
public class CallbackDispatcher extends Dispatcher {
    @Autowired
    FindRideRequestService findRideRequestService;
    @Autowired
    FindPassengerRequestService findPassengerRequestService;
    public void onCallbackReceived(CallbackQuery callbackQuery) {
        Message incomeMessage = callbackQuery.getMessage();
        String callbackData = callbackQuery.getData();
        Long chatId = incomeMessage.getChatId();
        log.info("Get callbackData: " + callbackData);
        if (callbackData.contains("-")) {
            String handler = getHandler(callbackData);
            String callback = getProcess(callbackData);
            log.info("Get callBack: " + callbackData + ", Handler: " + handler);
            switch (handler) {
                case "START" -> startHandler.handleReceivedCallback(callback, incomeMessage);
                case "ADMIN" -> adminHandler.handleReceivedCallback(callback, incomeMessage);
                case "USER" -> userHandler.handleReceivedCallback(callback, incomeMessage);
                case "CAR" -> carHandler.handleReceivedCallback(callback, incomeMessage);
                case "FIND_RIDE" -> findRideHandler.handleReceivedCallback(callback, incomeMessage);
                case "FIND_PAS" -> findPassengerHandler.handleReceivedCallback(callback, incomeMessage);
            }
        } else {
            String callback = "";
            switch (callbackData) {
                case "EDIT_LAST_REQUEST", "CANCEL_LAST_REQUEST" -> {
                    if (callbackData.equals("EDIT_LAST_REQUEST")) callback = "EDIT_REQUEST_START:";
                    else callback = "CANCEL_REQUEST:";
                    if (findPassengerRequestService.findLastUserRequestOptional(chatId).isPresent() && findRideRequestService.findLastUserRequestOptional(chatId).isPresent()) {
                        LocalDateTime findPassengerRequestCreatedAt = findPassengerRequestService.findLastUserRequest(chatId).getCreatedAt();
                        LocalDateTime findRideRequestCreatedAt = findRideRequestService.findLastUserRequest(chatId).getCreatedAt();
                        if (findPassengerRequestCreatedAt.isAfter(findRideRequestCreatedAt))
                            findPassengerHandler.handleReceivedCallback(callback + findPassengerRequestService.findLastUserRequest(chatId).getId(), incomeMessage);
                        else
                            findRideHandler.handleReceivedCallback(callback + findRideRequestService.findLastUserRequest(chatId).getId(), incomeMessage);
                    } else if (findPassengerRequestService.findLastUserRequestOptional(chatId).isPresent()) {
                        findPassengerHandler.handleReceivedCallback(callback + findPassengerRequestService.findLastUserRequest(chatId).getId(), incomeMessage);

                    } else if (findRideRequestService.findLastUserRequestOptional(chatId).isPresent()) {
                        findRideHandler.handleReceivedCallback(callback + findRideRequestService.findLastUserRequest(chatId).getId(), incomeMessage);

                    } else findPassengerHandler.handleReceivedCallback("NO_ACTIVE_REQUEST", incomeMessage);
                }
            }
        }
    }
}
