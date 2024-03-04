package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.servise.Extractor;
import by.ivam.fellowtravelerbot.servise.handler.FindPassengerHandler;
import by.ivam.fellowtravelerbot.servise.handler.FindRideHandler;
import by.ivam.fellowtravelerbot.servise.handler.MatchingHandler;
import by.ivam.fellowtravelerbot.servise.handler.MessageHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Service
@Log4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisMessageHandler extends MessageHandler {
    @Autowired
    private FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    private FindRideRequestRedisService findRideRequestRedisService;
    @Autowired
    private FindPassengerHandler findPassengerHandler;
    @Autowired
    private FindRideHandler findRideHandler;
    @Autowired
    private MatchingHandler matchingHandler;

    private final String FIND_PASSENGER_REQUEST = "find_passenger_request";
    private final String FIND_RIDE_REQUEST = "find_ride_request";
    private final String BOOKING = "booking";


    private SendMessage sendMessage;


    public void handleMessage(String event, String message) {
        String type = Extractor.extractParameter(message, Extractor.INDEX_ZERO);
        String id = Extractor.extractParameter(message, Extractor.INDEX_ONE);
//        log.debug("new event: " + event + ", request type: " + type + ", id: " + id);
        log.debug("new event: %s, request type: %s, id: %s".formatted(event, type, id) );
        switch (event) {
            case "hset" -> {
                handleHset(type, id);
            }
            case "expired" -> {
                handleExpired(type, id);
            }
            case "del" -> {
                handleDel(type, id);
            }
        }
    }
    // Handle events of creating new Redis hashes
    private void handleHset(String type, String id) {
        log.debug("method handleHset");
        switch (type) {
            case FIND_PASSENGER_REQUEST -> {
                if (!bookingService.hasBooking(getRequestType(type), id)) {
                    FindPassRequestRedis recentRequest = findPassRequestRedisService.findById(id);
                    List<Integer> matches = findRideRequestRedisService.findMatches(recentRequest);
                    long chatId = recentRequest.getChatId();
                    matchingHandler.sendListOfSuitableFindRideRequestMessage(matches, recentRequest.getRequestId(), chatId);
                }
            }
            case FIND_RIDE_REQUEST -> {
                if (!bookingService.hasBooking(getRequestType(type), id)) {
                    FindRideRequestRedis recentRequest = findRideRequestRedisService.findById(id);
                    List<Integer> matches = findPassRequestRedisService.findMatches(recentRequest);
                    long chatId = recentRequest.getChatId();
                    matchingHandler.sendListOfSuitableFindPassengerRequestMessage(matches, recentRequest.getRequestId(), chatId);
                }
            }
            case BOOKING -> {
                Booking booking = bookingService.findById(id);
                if (bookingService.isNewBooking(booking))
                    matchingHandler.sendBookingAnnouncementMessage(booking);
            }
        }
    }
    // Handle events of expire Time To Live keys
    private void handleExpired(String type, String idString) {
// TODO Удаление брони если существует с посланием соответствующего сообщения
        log.debug("method handleExpired");
//                int requestId = Extractor.extractId(message, Extractor.INDEX_ONE);
        int requestId = Integer.valueOf(idString);
        if (type.equals(FIND_PASSENGER_REQUEST)) {
            findPassengerRequestService.disActivateExpiredRequestById(requestId);
            findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
        } else if (type.equals(FIND_RIDE_REQUEST)) {

            findRideRequestService.disActivateRequestById(requestId);
            findRideHandler.sendExpireDepartureTimeMessage(requestId);
        }
    }
    // Handle events of deleting of Redis hashes
    private void handleDel(String type, String id) {
        log.debug("method handleDel");
        if (type.equals(BOOKING)) {
            BookingTemp bookingTemp = bookingTempService.findById(id).orElseThrow();
            RequestsType canceledBy = bookingTemp.getCanceledBy();
            if (canceledBy != null) { // TODO Проверить как сетится это значение
                if (canceledBy.equals(RequestsType.FIND_PASSENGER_REQUEST)) {
                    FindRideRequestRedis findRideRequestRedis = findRideRequestRedisService.findById(String.valueOf(bookingTemp.getFindPassengerRequestId()));
                    onCancelBookingByDriver(findRideRequestRedis);
                } else {
                    FindPassRequestRedis findPassRequestRedis = findPassRequestRedisService.findById(String.valueOf(bookingTemp.getFindPassengerRequestId()));
                    onCancelBookingByPassenger(findPassRequestRedis);
                }
            }
        }
    }

    private RequestsType getRequestType(String type) {
        RequestsType requestsType = RequestsType.NOT_REQUEST;
        if (type.equals(FIND_PASSENGER_REQUEST)) requestsType = RequestsType.FIND_PASSENGER_REQUEST;
        else if (type.equals(FIND_RIDE_REQUEST)) requestsType = RequestsType.FIND_RIDE_REQUEST;
        else log.error("type %s mismatch any request type");
        return requestsType;
    }

    private void onCancelBookingByDriver(FindRideRequestRedis findRideRequestRedis) {
        List<Integer> matches = findPassRequestRedisService.findMatches(findRideRequestRedis);
        long chatId = findRideRequestRedis.getChatId();
        matchingHandler.sendCancelingBookingMessage(chatId);
        matchingHandler.sendListOfSuitableFindPassengerRequestMessage(matches, findRideRequestRedis.getRequestId(), chatId);
    }

    private void onCancelBookingByPassenger(FindPassRequestRedis findPassRequestRedis) {
        List<Integer> matches = findRideRequestRedisService.findMatches(findPassRequestRedis);
        long chatId = findPassRequestRedis.getChatId();
        matchingHandler.sendCancelingBookingMessage(chatId);
        matchingHandler.sendListOfSuitableFindRideRequestMessage(matches, findPassRequestRedis.getRequestId(), chatId);
    }

}

