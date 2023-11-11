package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.MatchingOperation;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.servise.Extractor;
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
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Log4j
@Service
public class MatchingHandler extends MessageHandler implements HandlerInterface {
    @Autowired
    FindRideHandler findRideHandler;
    @Autowired
    FindPassengerHandler findPassengerHandler;


    EditMessageText editMessage = new EditMessageText();
    SendMessage sendMessage = new SendMessage();

    private final String handlerPrefix = Handlers.MATCHING.getHandlerPrefix();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        String messageText = incomeMessage.getText();
        Long chatId = incomeMessage.getChatId();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus + ". message: " + messageText);
        String process = chatStatus;
        if (chatStatus.contains(":")) process = Extractor.extractProcess(chatStatus);
//        if (chatStatus.contains(":")) process = extractProcess(chatStatus);
        switch (process) {

        }
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug("method handleReceivedCallback. get callback: " + callback);
        Long chatId = incomeMessage.getChatId();
        String process = callback;
        if (callback.contains(":")) {
            process = Extractor.extractProcess(callback);
//            process = extractProcess(callback);
        }
        switch (process) {
            case "ACCEPT_FIND_RIDE_REQUEST" -> {
log.debug("");
            }
              case "ACCEPT_FIND_PASS_REQUEST" -> {

            }

        }

    }

    public void sendListOfSuitableFindRideRequestMessage(List<Integer> requestIdList, FindPassRequestRedis receivedRequest) {
        log.debug("method: sendListOfSuitableRideRequestMessage");
        String requestListsToString = findRideRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.ACCEPT_FIND_RIDE_REQUEST_CALLBACK.getValue(), receivedRequest.getRequestId());
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage = createListOfSuitableRequestsMessage(receivedRequest.getChatId(), requestListsToString, buttonsAttributesList);

        sendBotMessage(sendMessage);
    }

    public void sendListOfSuitableFindPassengerRequestMessage(List<Integer> requestIdList, FindRideRequestRedis receivedRequest) {
        log.debug("method: sendListOfSuitableFindPassengerRequestMessage");
        String requestListsToString = findPassengerRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.ACCEPT_FIND_PASS_REQUEST_CALLBACK.getValue(), receivedRequest.getRequestId());
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage = createListOfSuitableRequestsMessage(receivedRequest.getChatId(), requestListsToString, buttonsAttributesList);

        sendBotMessage(sendMessage);
    }


    private SendMessage createListOfSuitableRequestsMessage(long chatId, String requestsList, List<Pair<String, String>> buttonsAttributesList) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getSUITABLE_REQUESTS_LIST_MESSAGE(), requestsList));
        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        return sendMessage;
    }

    //    TODO сделать рефакторинг одноименных методов в хендлерах поиска поездок и пассажиров
    private String findRideRequestListsToString(List<Integer> requestsIdList) {
        List<FindRideRequest> requests = findRideRequestService.requestListByIdList(requestsIdList);
        if (requests.isEmpty()) {
            return messages.getFIND_RIDE_NO_ACTIVE_REQUEST_MESSAGE();
        } else {
            StringBuilder text = new StringBuilder();
            for (FindRideRequest request : requests) {
                int n = requests.indexOf(request) + 1;
                text.append(n).append(". ").append(findRideHandler.requestToString(request)).append("\n");
            }
            return text.toString();
        }
    }

    //    TODO сделать рефакторинг одноименных методов в хендлерах поиска поездок и пассажиров
    private String findPassengerRequestListsToString(List<Integer> requestsIdList) {
        List<FindPassengerRequest> requests = findPassengerRequestService.requestListByIdList(requestsIdList);
        if (requests.isEmpty()) {
            return messages.getFIND_RIDE_NO_ACTIVE_REQUEST_MESSAGE();
        } else {
            StringBuilder text = new StringBuilder();
            for (FindPassengerRequest request : requests) {
                int n = requests.indexOf(request) + 1;
                text.append(n).append(". ").append(findPassengerHandler.requestToString(request)).append("\n");
            }
            return text.toString();
        }
    }

    //    TODO сделать рефакторинг одноименных методов в хендлерах поиска поездок и пассажиров
    private List<Pair<String, String>> requestButtonsAttributesListCreator(List<Integer> requestsIdList, String callbackData) {
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        if (requestsIdList.isEmpty()) {
            buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        } else {
            Map<Integer, String> requestButtonsAttributes = requestsIdList
                    .stream()
                    .collect(Collectors.toMap(id -> id, request -> String.valueOf(requestsIdList.indexOf(request) + 1)));
            buttonsAttributesList = buttons.buttonsAttributesListCreator(requestButtonsAttributes, callbackData);
            buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        }
        return buttonsAttributesList;
    }
}
