package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.MatchingOperation;
import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.model.Ride;
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
import java.util.Set;
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
        if (callback.contains(":")) process = Extractor.extractProcess(callback);

        switch (process) {
            case "BOOK_REQUEST_CALLBACK" -> {
                String initiator = Extractor.extractParameter(callback, Extractor.INDEX_ONE);
                String firstId = Extractor.extractParameter(callback, Extractor.INDEX_TWO);
                String secondId = Extractor.extractParameter(callback, Extractor.INDEX_THREE);

                Pair<FindPassRequestRedis, FindRideRequestRedis> pairOfRequests = getPairOfRequests(firstId, secondId, initiator);
                bookingService.addBooking(pairOfRequests, initiator);

                editMessage = sendNoticeAboutSendingBookingMessage(incomeMessage);
            }
            case "ACCEPT_BOOKING" -> {
                log.debug("ACCEPT_BOOKING - " + callback);
                String bookingId = Extractor.extractParameter(callback, Extractor.INDEX_ONE);
                Ride ride = matchService.createOrUpdateRide(bookingId);
                editMessage = sendCreateRideNoticeMessage(incomeMessage, ride);
                matchService.deleteBooking(bookingId);
            }
            case "DENY_BOOKING" -> {
                String bookingId = Extractor.extractParameter(callback, Extractor.INDEX_ONE);
                onDenyBooking(bookingId);
                editMessage = sendReplyDenyBookingMessage(incomeMessage);
            }
        }
        sendEditMessage(editMessage);
    }

    public void sendListOfSuitableFindRideRequestMessage(List<Integer> requestIdList, String requestId, long chatId) {
        log.debug("method: sendListOfSuitableRideRequestMessage");
        String requestListsToString = findRideRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.BOOK_REQUEST_CALLBACK.getValue(),
                RequestsType.FIND_PASSENGER_REQUEST.getValue(), requestId);
        List<Pair<String, String>> buttonsAttributesList = requestButtonsAttributesListCreator(requestIdList, callback);
        sendMessage =
                createListOfSuitableRequestsMessage(chatId, requestListsToString, buttonsAttributesList);
        sendBotMessage(sendMessage);
    }

    public void sendListOfSuitableFindPassengerRequestMessage(List<Integer> requestIdList, String requestId, long chatId) {
        log.debug("method: sendListOfSuitableFindPassengerRequestMessage");
        //Todo изменить сообщение
        String requestListsToString = findPassengerRequestListsToString(requestIdList);
        String callback = handlerPrefix + String.format(MatchingOperation.BOOK_REQUEST_CALLBACK.getValue(),
                RequestsType.FIND_RIDE_REQUEST.getValue(), requestId);
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
        //TODO содержимое if-else вынести в отдельные методы
        if (initiator.equals(RequestsType.FIND_PASSENGER_REQUEST.getValue())) {
            sendMessage.setChatId(booking.getFindRideRequestRedis().getChatId());
            int findPassRequestId = Integer.valueOf(booking.getFindPassRequestRedis().getRequestId());
            FindPassengerRequest requestToSend = findPassengerRequestService.findById(findPassRequestId);
            String requestToString = findPassengerHandler.requestToString(requestToSend);
            sendMessage.setText(String.format(messages.getBOOKING_RESPONSE_MESSAGE(), requestToString));

        } else if (initiator.equals(RequestsType.FIND_RIDE_REQUEST.getValue())){
            /*
            TODO вызывает java.lang.NullPointerException: Cannot invoke "by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis.getChatId()" because the return value of "by.ivam.fellowtravelerbot.redis.model.Booking.getFindPassRequestRedis()" is null
             при бронипрвании со стороны пассажира
             роверить работу изменений
            */
            sendMessage.setChatId(booking.getFindRideRequestRedis().getChatId());
            int findRideRequestId = Integer.valueOf(booking.getFindRideRequestRedis().getRequestId());
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
//        TODO проверить отсылаемое сообщение
    }

    private void sendNoticeAboutDenyBookingMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getBOOKING_DENY_MESSAGE());
        sendMessage.setReplyMarkup(null); // need to set null to remove no longer necessary inline keyboard
        sendBotMessage(sendMessage);
        log.debug("method sendNoticeAboutDenyBookingMessage");
    }

    private EditMessageText sendReplyDenyBookingMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getBOOKING_DENY_REPLY_MESSAGE());
        editMessage.setReplyMarkup(null); // need to set null to remove no longer necessary inline keyboard
        log.debug("method sendReplyDenyBookingMessage");
        return editMessage;
    }

    public void onDenyBooking(String bookingId) {
        log.debug("method onDenyBookingDeny");
        Booking booking = matchService.getBooking(bookingId);
        FindPassRequestRedis findPassRequestRedis = booking.getFindPassRequestRedis();
        FindRideRequestRedis findRideRequestRedis = booking.getFindRideRequestRedis();
        long driverChatId = findPassRequestRedis.getChatId();
        long passengerChatId = findRideRequestRedis.getChatId();

        if (booking.getInitiator().equals(RequestsType.FIND_PASSENGER_REQUEST.getValue())) {
            sendNoticeAboutDenyBookingMessage(driverChatId);                 // notify booking initiator
            List<Integer> matches = matchService.getFindRideRequestMatches(findPassRequestRedis);
            sendListOfSuitableFindRideRequestMessage(matches, findPassRequestRedis.getRequestId(), driverChatId);
        } else {
            sendNoticeAboutDenyBookingMessage(passengerChatId); // notify initiator
            List<Integer> matches = matchService.getFindPassRequestMatches(findRideRequestRedis);
            sendListOfSuitableFindPassengerRequestMessage(matches, findRideRequestRedis.getRequestId(), passengerChatId);
        }

        // TODO Set cancel initiator to BookingTemp
        matchService.deleteBooking(bookingId);
    }

    public void sendCancelingBookingMessage(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getBOOKING_CANCEL_MESSAGE());
        sendMessage.setReplyMarkup(null); // need to set null to remove no longer necessary inline keyboard
        sendBotMessage(sendMessage);
        log.debug("method sendCancelingBookingMessage");
    }

    private EditMessageText sendCreateRideNoticeMessage(Message incomeMessage, Ride ride) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getRIDE_MESSAGE());
        editMessage.setReplyMarkup(null); // need to set null to remove no longer necessary inline keyboard
// TODO После создания state machine удалить верхние строки и сделать возврат void
        String rideToString = String.format(messages.getCREATE_RIDE_MESSAGE(), rideToString(ride));
        FindPassengerRequest findPassengerRequest = ride.getFindPassengerRequest();
        Set<FindRideRequest> findRideRequestSet = ride.getFindRideRequests();
        sendRideToDriver(findPassengerRequest, rideToString);
        sendRideToPassengers(findRideRequestSet, rideToString);
        return editMessage;
    }


    private void sendRideToDriver(FindPassengerRequest request, String messageText) {
        sendMessage.setChatId(request.getUser().getChatId());
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(null); // TODO создать кнопки
        sendBotMessage(sendMessage);
    }

    private void sendRideToPassengers(Set<FindRideRequest> findRideRequestSet, String messageText) {
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(null); // TODO создать кнопки
        List<Long> chatIdList = findRideRequestSet.stream()
                .map(rideRequest -> rideRequest.getUser().getChatId())
                .collect(Collectors.toList());
        for (Long chatId : chatIdList) {
            sendMessage.setChatId(chatId);
        }
    }

    private String rideToString(Ride ride) {
        List<FindRideRequest> findRideRequestList = ride.getFindRideRequests()
                .stream()
                .collect(Collectors.toList());
        StringBuilder text = new StringBuilder();
        text.append(findPassengerHandler.requestToString(ride.getFindPassengerRequest())).append("\n\n");
        for (FindRideRequest request : findRideRequestList) {
            int n = findRideRequestList.indexOf(request) + 1;
            text.append(n).append(". ").append(findRideHandler.requestToString(request)).append("\n");
        }
        return text.toString();
    }

    //    TODO сделать рефакторинг одноименных методов в хендлерах поиска поездок и пассажиров
    private String findRideRequestListsToString(List<Integer> requestsIdList) {
        List<FindRideRequest> requests = findRideRequestService.requestListByIdList(requestsIdList);
        if (requests.isEmpty()) {
            return messages.getNO_SUITABLE_REQUEST_MESSAGE();
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

    private Pair<FindPassRequestRedis, FindRideRequestRedis> getPairOfRequests(String firstId, String secondId, String initiator) {
        FindPassRequestRedis findPassRequestRedis;
        FindRideRequestRedis findRideRequestRedis;
        if (initiator.equals(RequestsType.FIND_PASSENGER_REQUEST.getValue())) {
            findPassRequestRedis = findPassRequestRedisService.findById(firstId);
            findRideRequestRedis = findRideRequestRedisService.findById(secondId);
        } else {
            findRideRequestRedis = findRideRequestRedisService.findById(firstId);
            findPassRequestRedis = findPassRequestRedisService.findById(secondId);
        }
        return Pair.of(findPassRequestRedis, findRideRequestRedis);
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
