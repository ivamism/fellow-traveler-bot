package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.enums.BookingInitiator;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.MatchingOperation;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.FindPassRequestRedisService;
import by.ivam.fellowtravelerbot.redis.service.FindRideRequestRedisService;
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

    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    FindRideRequestRedisService findRideRequestRedisService;


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
                String firstId = Extractor.extractParameter(callback, Extractor.INDEX_TWO);
                String secondId = Extractor.extractParameter(callback, Extractor.INDEX_THREE);
                matchService.addBooking(firstId, secondId, initiator);
                editMessage = sendNoticeAboutSendingBookingMessage(incomeMessage);
//                TODO отправить сообщение о том что выслан запрос на бронирование
            }
            case "ACCEPT_BOOKING" -> {
                log.debug("ACCEPT_BOOKING - " + callback);
                nextStep(incomeMessage);
            }
            case "DENY_BOOKING" -> {
                String bookingId = Extractor.extractParameter(callback, Extractor.INDEX_ONE);
                onDenyBooking(bookingId);
//                sendNoticeAboutDenyBookingMessage(bookingId);
//                matchService.deleteBooking(bookingId);
//                log.debug("DENY_BOOKING - " + callback);
                editMessage = sendReplyDenyBookingMessage(incomeMessage);
//                sendReplyDenyBookingMessage(incomeMessage);

            }
        }
        sendEditMessage(editMessage);
    }

    public void sendListOfSuitableFindRideRequestMessage(List<Integer> requestIdList, FindPassRequestRedis receivedRequest, long chatId) {
        log.debug("method: sendListOfSuitableRideRequestMessage");
        String requestListsToString = findRideRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.BOOK_REQUEST_CALLBACK.getValue(),
                BookingInitiator.FIND_PASSENGER_REQUEST.getValue(), receivedRequest.getRequestId());
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage =
                createListOfSuitableRequestsMessage(chatId, requestListsToString, buttonsAttributesList);
        sendBotMessage(sendMessage);
    }

    public void sendListOfSuitableFindPassengerRequestMessage(List<Integer> requestIdList, FindRideRequestRedis receivedRequest, long chatId) {
        log.debug("method: sendListOfSuitableFindPassengerRequestMessage");
        //Todo изменить сообщение
        String requestListsToString = findPassengerRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.BOOK_REQUEST_CALLBACK.getValue(),
                BookingInitiator.FIND_RIDE_REQUEST.getValue(), receivedRequest.getRequestId());//
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage =
                createListOfSuitableRequestsMessage(chatId, requestListsToString, buttonsAttributesList);
        sendBotMessage(sendMessage);
    }


    private SendMessage createListOfSuitableRequestsMessage(long chatId, String requestsList, List<Pair<String, String>> buttonsAttributesList) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(messages.getSUITABLE_REQUESTS_LIST_MESSAGE(), requestsList));
        sendMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        log.debug("method: createListOfSuitableRequestsMessage");
        return sendMessage;
    }

    public void sendBookingAnnouncementMessage(Booking booking) {
        log.debug("method sendBookingAnnouncementMessage");
        String initiator = booking.getInitiator();
        String bookingId = booking.getId();
        if (initiator.equals(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())) {

            sendMessage.setChatId(booking.getFindRideRequestRedis().getChatId());
            int findPassRequestId = Integer.parseInt(booking.getFindPassRequestRedis().getRequestId());
            FindPassengerRequest requestToSend = findPassengerRequestService.findById(findPassRequestId);
            String requestToString = findPassengerHandler.requestToString(requestToSend);
            sendMessage.setText(String.format(messages.getBOOKING_RESPONSE_MESSAGE(), requestToString));
        } else {
            sendMessage.setChatId(booking.getFindPassRequestRedis().getChatId());
            int findRideRequestId = Integer.parseInt(booking.getFindRideRequestRedis().getRequestId());
            FindRideRequest requestToSend = findRideRequestService.findById(findRideRequestId);
            String requestToString = findRideHandler.requestToString(requestToSend);
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

    private EditMessageText sendNoticeAboutSendingBookingMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getNOTICE_ABOUT_SENDING_BOOKING_MESSAGE());
        log.debug("method sendNoticeAboutSendingBookingMessage");
        return editMessage;
    }

    private void sendNoticeAboutDenyBookingMessage(String bookingId) {
        Booking booking = matchService.getBooking(bookingId);
        if (booking.getInitiator().equals(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())) {
            sendMessage.setChatId(booking.getFindRideRequestRedis().getChatId());
        } else sendMessage.setChatId(booking.getFindPassRequestRedis().getChatId());
        sendMessage.setText(messages.getBOOKING_DENY_MESSAGE());
        sendMessage.setReplyMarkup(null);
        sendBotMessage(sendMessage);
        log.debug("method sendNoticeAboutDenyBookingMessage");
    }

    private void sendNoticeAboutDenyBookingMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getBOOKING_DENY_MESSAGE());
        sendMessage.setReplyMarkup(null);
        sendBotMessage(sendMessage);
        log.debug("method sendNoticeAboutDenyBookingMessage");
    }

        private EditMessageText sendReplyDenyBookingMessage(Message incomeMessage) {
//    private void sendReplyDenyBookingMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getBOOKING_DENY_REPLY_MESSAGE());
        editMessage.setReplyMarkup(null);
        log.debug("method sendReplyDenyBookingMessage");
        return editMessage;
//        sendEditMessage(editMessage);
    }

    public void onDenyBooking(String bookingId) {
        log.debug("method onDenyBookingDeny");
        Booking booking = matchService.getBooking(bookingId);
        FindPassRequestRedis findPassRequestRedis = booking.getFindPassRequestRedis();
        FindRideRequestRedis findRideRequestRedis = booking.getFindRideRequestRedis();
        long driverChatId = findPassRequestRedis.getChatId();
        long passengerChatId = findRideRequestRedis.getChatId();
        if (booking.getInitiator().equals(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())) {
            sendNoticeAboutDenyBookingMessage(driverChatId); // notify booking initiator
            List<Integer> matches = matchService.getFindRideRequestMatches(findPassRequestRedis);
            sendListOfSuitableFindRideRequestMessage(matches, findPassRequestRedis, driverChatId);
        } else {
            sendNoticeAboutDenyBookingMessage(passengerChatId); // notify initiator
            List<Integer> matches = matchService.getFindPassRequestMatches(findRideRequestRedis);
            sendListOfSuitableFindPassengerRequestMessage(matches, findRideRequestRedis, passengerChatId);
        }
        matchService.deleteBooking(bookingId);
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
