package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.MatchingOperation;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
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
public class MatchingHandler extends BaseHandler implements HandlerInterface {
    @Autowired
    FindRideHandler findRideHandler;
    @Autowired
    FindPassengerHandler findPassengerHandler;


    EditMessageText editMessage = new EditMessageText();
    SendMessage sendMessage = new SendMessage();

    private final String handlerPrefix = Handlers.MATCHING.getHandlerPrefix();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        log.debug(" method : handleReceivedMessage");
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        log.debug(" method : handleReceivedCallback");

    }

    public void sendListOfSuitableFindRideRequestMessage(List<Integer> requestIdList, long chatId) {
        log.debug("method: sendListOfSuitableRideRequestMessage");
        String requestListsToString = findRideRequestListsToString(requestIdList);
        String callback = handlerPrefix + MatchingOperation.ACCEPT_FIND_RIDE_REQUEST_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage = createListOfSuitableRequestsMessage(chatId, requestListsToString, buttonsAttributesList);

        sendBotMessage(sendMessage);
    }

    public void sendListOfSuitableFindPassengerRequestMessage(List<Integer> requestIdList, long chatId) {
        log.debug("method: sendListOfSuitableFindPassengerRequestMessage");
        String requestListsToString = findPassengerRequestListsToString(requestIdList);
        String callback = handlerPrefix + MatchingOperation.ACCEPT_FIND_RIDE_REQUEST_CALLBACK.getValue();
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage = createListOfSuitableRequestsMessage(chatId, requestListsToString, buttonsAttributesList);

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
