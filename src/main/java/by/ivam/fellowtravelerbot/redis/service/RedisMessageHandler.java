package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.servise.*;
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
        String idString = Extractor.extractParameter(message, Extractor.INDEX_ONE);
        switch (event) {
            case "hset" -> {
                switch (type) {
                    case FIND_PASSENGER_REQUEST -> {
                        log.debug("new event: " + event + ", request type: " + type + ", id: " + idString);
                        FindPassRequestRedis recentRequest = findPassRequestRedisService.findById(idString);
                        List<Integer> matches = findRideRequestRedisService.findMatches(recentRequest);
                        long chatId = recentRequest.getChatId();
                        matchingHandler.sendListOfSuitableFindRideRequestMessage(matches, recentRequest, chatId);
                    }
                    case FIND_RIDE_REQUEST -> {
                        log.debug("new event: " + event + ", request type: " + type + ", id: " + idString);
                        FindRideRequestRedis recentRequest = findRideRequestRedisService.findById(idString);
                        List<Integer> matches = findPassRequestRedisService.findMatches(recentRequest);
                        long chatId = recentRequest.getChatId();
                        matchingHandler.sendListOfSuitableFindPassengerRequestMessage(matches, recentRequest, chatId);
                    }
                    case BOOKING -> {
                        log.debug("new event: " + event + ", request type: " + type + ", id: " + idString);
                        Booking booking = bookingService.findById(idString);
                        if (bookingService.isNewRequest(booking))
                            matchingHandler.sendBookingAnnouncementMessage(booking);
                    }
                }
            }
            case "expired" -> {
                int requestId = Extractor.extractId(message, Extractor.INDEX_ONE);
                if (type.equals(FIND_PASSENGER_REQUEST)) {
                    log.debug("new event: " + event + ", request type: " + type + ", id: " + requestId);
                    findPassengerRequestService.disActivateExpiredRequestById(requestId);
                    findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
                } else if (type.equals(FIND_RIDE_REQUEST)) {
                    log.debug("new event: " + event + ", request type: " + type + ", id: " + idString);
                    findRideRequestService.disActivateRequestById(requestId);
                    findRideHandler.sendExpireDepartureTimeMessage(requestId);
                }
            }
            case "del" -> {
                log.debug("new event " + event + ", request type: " + type + ", id: " + idString);
                if (type.equals(BOOKING)) {
                    BookingTemp bookingTemp = bookingTempService.findById(idString).orElseThrow();
                    RequestsType canceledBy = bookingTemp.getCanceledBy();
                    if (canceledBy != null) {
                        long chatId;
                        List<Integer> matches;
                        FindPassRequestRedis findPassRequestRedis = findPassRequestRedisService.findById(String.valueOf(bookingTemp.getFindPassengerRequestId()));
                        FindRideRequestRedis findRideRequestRedis = findRideRequestRedisService.findById(String.valueOf(bookingTemp.getFindPassengerRequestId()));
                        if (canceledBy.equals(RequestsType.FIND_PASSENGER_REQUEST)) {
                            matches = findPassRequestRedisService.findMatches(findRideRequestRedis);
                            chatId = findRideRequestRedis.getChatId();
                            matchingHandler.sendCancelingBookingMessage(chatId);
                            matchingHandler.sendListOfSuitableFindPassengerRequestMessage(matches, findRideRequestRedis, chatId);
                        } else {
                            matches = findRideRequestRedisService.findMatches(findPassRequestRedis);
                            chatId = findPassRequestRedis.getChatId();
                            matchingHandler.sendCancelingBookingMessage(chatId);
                            matchingHandler.sendListOfSuitableFindRideRequestMessage(matches, findPassRequestRedis, chatId);
                        }
                    }
                }
            }
        }
    }
}

