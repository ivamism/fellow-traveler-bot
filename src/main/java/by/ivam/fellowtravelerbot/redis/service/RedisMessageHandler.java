package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.servise.handler.FindPassengerHandler;
import by.ivam.fellowtravelerbot.servise.handler.FindRideHandler;
import by.ivam.fellowtravelerbot.servise.handler.MatchingHandler;
import by.ivam.fellowtravelerbot.servise.handler.MessageHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static by.ivam.fellowtravelerbot.bot.enums.RequestsType.*;
import static by.ivam.fellowtravelerbot.bot.enums.RequestsType.FIND_PASSENGER_REQUEST;


@EqualsAndHashCode(callSuper = true)
@Service
@Log4j
@Data
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


    private final String EVENT_NEW_HSET = "hset"; //
    private final String EVENT_EXPIRED = "expired";
    private final String EVENT_DELETED = "del";

    private final String TYPE_FIND_PASSENGER_REQUEST = "find_passenger_request";
    private final String TYPE_FIND_RIDE_REQUEST = "find_ride_request";
    private final String TYPE_BOOKING = "booking";

    // Handle events caught by Redis MessageListener
    public void handleEvent(String event, String message) {

        String type = extractParameter(message, ZERO_VALUE);
        String id = extractParameter(message, FIRST_VALUE);
        log.debug("new event: %s, type: %s, id: %s".formatted(event, type, id));
        final RequestsType requestType = getRequestType(type);

        switch (event) {
            case EVENT_NEW_HSET -> handleHset(type, id);
            case EVENT_EXPIRED -> handleExpired(requestType, id);
            case EVENT_DELETED -> handleDel(type, id);
        }
    }

    // Handle events of creating new Redis hashes
    private void handleHset(String type, String id) {
        log.debug("method handleHset");
        final RequestsType requestType = getRequestType(type);
//        if (!bookingService.hasBooking(requestType, id))
        switch (type) {
            case TYPE_FIND_PASSENGER_REQUEST -> {
                if (!bookingService.hasBooking(requestType, id)) {
                    FindPassRequestRedis recentRequest = findPassRequestRedisService.findById(id);
                    List<Integer> matches = findRideRequestRedisService.findMatches(recentRequest);
                    long chatId = recentRequest.getChatId();
                    matchingHandler.sendListOfSuitableFindRideRequestMessage(matches, recentRequest.getRequestId(), chatId);
                }
            }
            case TYPE_FIND_RIDE_REQUEST -> {
                if (!bookingService.hasBooking(requestType, id)) {
                    FindRideRequestRedis recentRequest = findRideRequestRedisService.findById(id);
                    List<Integer> matches = findPassRequestRedisService.findMatches(recentRequest);
                    long chatId = recentRequest.getChatId();
                    matchingHandler.sendListOfSuitableFindPassengerRequestMessage(matches, recentRequest.getRequestId(), chatId);
                }
            }
            case TYPE_BOOKING -> {
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
        int requestId = Integer.valueOf(idString);
        final RequestsType requestType = getRequestType(type);
//        if (!bookingService.hasBooking(requestType, id))
        if (type.equals(TYPE_FIND_PASSENGER_REQUEST)) {
            findPassengerRequestService.disActivateExpiredRequestById(requestId);
            findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
//            if (bookingService.hasBooking(FIND_PASSENGER_REQUEST, idString))
//                onExpiringRequest(FIND_PASSENGER_REQUEST, requestId);
        } else if (type.equals(TYPE_FIND_RIDE_REQUEST)) {
            findRideRequestService.disActivateRequestById(requestId);
            findRideHandler.sendExpireDepartureTimeMessage(requestId);
        }
    }

    // Handle events of expire Time To Live keys
    private void handleExpired(RequestsType requestsType, String idString) {
// TODO Удаление брони если существует с посланием соответствующего сообщения
        log.debug("method handleExpired");
        int requestId = Integer.parseInt(idString);

        if (requestsType == FIND_PASSENGER_REQUEST) {
            findPassengerRequestService.disActivateExpiredRequestById(requestId);
            findPassengerHandler.sendExpireDepartureTimeMessage(requestId);

        } else if (requestsType == FIND_RIDE_REQUEST) {
            findRideRequestService.disActivateRequestById(requestId);
            findRideHandler.sendExpireDepartureTimeMessage(requestId);
        }

        if (bookingService.hasBooking(requestsType, idString))
            onExpiringRequest(requestsType, requestId);
//bookingService.removeBookingByCancelingRequest(requestsType, idString);
    }


    // Handles events of deleting of Redis hashes
    private void handleDel(String type, String id) {
        log.debug("method handleDel");
        if (type.equals(TYPE_BOOKING)) {
            handleBookingDeletion(id);
        }
    }

    // Handles events related to the deletion of bookings
    private void handleBookingDeletion(String bookingId) {
        log.debug("method handleBookingDeletion");
        BookingTemp bookingTemp;
        Optional<BookingTemp> bookingTempOptional = bookingTempService.findById(bookingId);
        if (bookingTempOptional.isPresent()) {
            bookingTemp = bookingTempOptional.get();
            Optional.ofNullable(bookingTemp.getCanceledBy()).ifPresent(requestsType -> deleteBookingByCanceling(requestsType, bookingTemp));
        } else log.debug("No further actions to handle on deleting booking: %s".formatted(bookingId));
    }

    // Actions if the booking is deleted due to cancellation of the request
    private void deleteBookingByCanceling(RequestsType canceledBy, BookingTemp bookingTemp) {
        log.debug("method deleteBookingByCanceling");
        if (canceledBy.equals(FIND_PASSENGER_REQUEST)) {
            findRideRequestRedisService
                    .getOptionalById(String.valueOf(bookingTemp.getFindRideRequestId()))
                    .ifPresent(findRideRequestRedis -> onCancelBookingByDriver(findRideRequestRedis));
        } else if (canceledBy.equals(FIND_RIDE_REQUEST)) {
            findPassRequestRedisService
                    .getOptionalById(String.valueOf(bookingTemp.getFindPassengerRequestId()))
                    .ifPresent(findPassRequestRedis -> onCancelBookingByPassenger(findPassRequestRedis));
        }
    }

    private RequestsType getRequestType(String type) {
        RequestsType requestsType = NOT_REQUEST;
        if (type.equals(TYPE_FIND_PASSENGER_REQUEST)) requestsType = FIND_PASSENGER_REQUEST;
        else if (type.equals(TYPE_FIND_RIDE_REQUEST)) requestsType = FIND_RIDE_REQUEST;
        else log.error("type %s mismatch any request type".formatted(type));
        return requestsType;
    }

    private void onExpiringRequest(RequestsType requestsType, int requestId) {
//bookingService.getBookingsToDeleteOnCancelingRequest(requestsType, String.valueOf(requestId))
//        .stream()
//        .forEach(booking -> bookingService.);
//        bookingService.removeBookingByCancelingRequest(requestsType, requestId);

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

