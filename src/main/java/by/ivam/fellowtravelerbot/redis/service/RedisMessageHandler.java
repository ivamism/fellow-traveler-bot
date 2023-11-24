package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.servise.Extractor;
import by.ivam.fellowtravelerbot.servise.FindPassengerRequestService;
import by.ivam.fellowtravelerbot.servise.FindRideRequestService;
import by.ivam.fellowtravelerbot.servise.MatchService;
import by.ivam.fellowtravelerbot.servise.handler.FindPassengerHandler;
import by.ivam.fellowtravelerbot.servise.handler.FindRideHandler;
import by.ivam.fellowtravelerbot.servise.handler.MatchingHandler;
import by.ivam.fellowtravelerbot.servise.handler.MessageHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Service
@Log4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisMessageHandler extends MessageHandler {
    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    FindRideRequestRedisService findRideRequestRedisService;
    @Autowired
    FindPassengerRequestService findPassengerRequestService;
    @Autowired
    FindRideRequestService findRideRequestService;

    @Autowired
    private  BookingService bookingService;
    @Autowired
    private FindPassengerHandler findPassengerHandler;
    @Autowired
    FindRideHandler findRideHandler;
    @Autowired
    MatchingHandler matchingHandler;
    @Autowired
    MatchService matchService;

    private final String FIND_PASSENGER_REQUEST = "find_passenger_request";
    private final String FIND_RIDE_REQUEST = "find_ride_request";
    private final String BOOKING = "booking";


    private SendMessage sendMessage;

    public void handleMessage(String event, String message) {
        String requestType = Extractor.extractParameter(message, Extractor.INDEX_ZERO);
        int requestId = Extractor.extractId(message, Extractor.INDEX_ONE);
        String requestIdString = Extractor.extractParameter(message, Extractor.INDEX_ONE);
        switch (event) {
            case "hset" -> {
                if (requestType.equals(FIND_PASSENGER_REQUEST)) {
                    log.debug("new event: " + event + ", request type: " + requestType + ", id: " + requestIdString);
//                    matchService.getNewFindPassengerRequest(requestIdString);
                    FindPassRequestRedis recentRequest = findPassRequestRedisService.findById(requestIdString);
                    List<Integer> matches = findRideRequestRedisService.findMatches(recentRequest);
                    matchingHandler.sendListOfSuitableFindRideRequestMessage(matches, recentRequest);
                } else if (requestType.equals(FIND_RIDE_REQUEST)) {
                    log.debug("new event: " + event + ", request type: " + requestType + ", id: " + requestIdString);
//                    matchService.getNewFindRideRequest(requestIdString);
                    FindRideRequestRedis recentRequest = findRideRequestRedisService.findById(requestIdString);
                    List<Integer> matches = findPassRequestRedisService.findMatches(recentRequest);
                    matchingHandler.sendListOfSuitableFindPassengerRequestMessage(matches, recentRequest);
                }else if (requestType.equals(BOOKING)) {
                    log.debug("new event: " + event + ", request type: " + requestType + ", id: " + requestIdString);
                    Booking booking = bookingService.findById(requestIdString);
                    if (bookingService.isNewRequest(booking))
                        matchingHandler.sendBookingAnnouncementMessage(booking);
                }

            }
            case "expired" -> {
                if (requestType.equals(FIND_PASSENGER_REQUEST)) {
                    log.debug("new event: " + event + ", request type: " + requestType + ", id: " + requestId);
                    findPassengerRequestService.disActivateRequestById(requestId);
                    findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
                } else if (requestType.equals(FIND_RIDE_REQUEST)) {
                    log.debug("new event: " + event + ", request type: " + requestType + ", id: " + requestIdString);
                    findRideRequestService.disActivateRequestById(requestId);
                    findRideHandler.sendExpireDepartureTimeMessage(requestId);
                }
            }
        }
    }
}
