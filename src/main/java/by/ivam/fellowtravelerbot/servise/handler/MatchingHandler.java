package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.enums.BookingInitiator;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.MatchingOperation;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.redis.model.Booking;
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
import org.telegram.telegrambots.meta.api.objects.polls.Poll;

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
    Poll poll = new Poll();

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

        }
        switch (process) {

            case "BOOK_REQUEST_CALLBACK" -> {
                String initiator = Extractor.extractParameter(callback, Extractor.INDEX_ONE);
                String findPassRequestId = Extractor.extractParameter(callback, Extractor.INDEX_TWO);
                String findRideRequestId = Extractor.extractParameter(callback, Extractor.INDEX_THREE);
                matchService.addBooking(findPassRequestId, findRideRequestId, initiator);
            }
            case "ACCEPT_BOOKING" -> {
                log.debug("ACCEPT_BOOKING - " + callback);
            }
            case "DENY_BOOKING" -> {
                log.debug("DENY_BOOKING - " + callback);
            }

        }

    }

    public void sendListOfSuitableFindRideRequestMessage(List<Integer> requestIdList, FindPassRequestRedis receivedRequest) {
        log.debug("method: sendListOfSuitableRideRequestMessage");
        String requestListsToString = findRideRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.BOOK_REQUEST_CALLBACK.getValue(),
                BookingInitiator.FIND_PASSENGER_REQUEST.getValue(), receivedRequest.getRequestId());
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage =
                createListOfSuitableRequestsMessage(receivedRequest.getChatId(), requestListsToString, buttonsAttributesList);

        sendBotMessage(sendMessage);
    }

    public void sendListOfSuitableFindPassengerRequestMessage(List<Integer> requestIdList, FindRideRequestRedis receivedRequest) {
        log.debug("method: sendListOfSuitableFindPassengerRequestMessage");
        String requestListsToString = findPassengerRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.BOOK_REQUEST_CALLBACK.getValue(),
                BookingInitiator.FIND_RIDE_REQUEST.getValue(), receivedRequest.getRequestId());//
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage =
                createListOfSuitableRequestsMessage(receivedRequest.getChatId(), requestListsToString, buttonsAttributesList);

        sendBotMessage(sendMessage);
    }


    private SendMessage createListOfSuitableRequestsMessage(long chatId, String requestsList, List<Pair<String, String>> buttonsAttributesList) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getSUITABLE_REQUESTS_LIST_MESSAGE(), requestsList));
        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));


        return sendMessage;
    }

    public void sendBookingAnnouncementMessage(Booking booking) {
        String initiator = booking.getInitiator();
        String bookingId = booking.getId();
        if (initiator.equals(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())) {
            sendMessage.setChatId(booking.getFindRideRequestRedis().getChatId());
            int findPassRequestId = Integer.parseInt(booking.getFindRideRequestRedis().getRequestId());
            String requestToString = findPassengerHandler.requestToString(findPassengerRequestService.findById(findPassRequestId));
            sendMessage.setText(String.format(messages.getBOOKING_RESPONSE_MESSAGE(), requestToString));
        } else {
            sendMessage.setChatId(booking.getFindPassRequestRedis().getChatId());
            int findRideRequestId = Integer.parseInt(booking.getFindPassRequestRedis().getRequestId());
            String requestToString = findRideHandler.requestToString(findRideRequestService.findById(findRideRequestId));
            sendMessage.setText(String.format(messages.getBOOKING_RESPONSE_MESSAGE(), requestToString));
        }
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and handlerPrefix)
        buttonsAttributesList.add(buttons.acceptButtonCreate(handlerPrefix
                + MatchingOperation.ACCEPT_BOOKING_CALLBACK.getValue() + bookingId)); // Start create button
        buttonsAttributesList.add(buttons.denyButtonCreate(handlerPrefix
                + MatchingOperation.DENY_BOOKING_CALLBACK.getValue() + bookingId)); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        sendBotMessage(sendMessage);
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
    protected void editMessageTextGeneralPreset(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
    }
    protected SendMessage nextStep(long chatId) {
//        TODO удалить метод по окончании реализации всего функционала
        sendMessage.setChatId(chatId);
        sendMessage.setText("nextStep");
        sendMessage.setReplyMarkup(null);
        log.debug("method: nextStep");
        return sendMessage;
    }

    protected EditMessageText nextStep(Message incomemessage) {
        //        TODO удалить метод по окончании реализации всего функционала
        editMessageTextGeneralPreset(incomemessage);
        editMessage.setText("nextStep");
        editMessage.setReplyMarkup(null);
        log.debug("method: nextStep");
        return editMessage;
    }
}
