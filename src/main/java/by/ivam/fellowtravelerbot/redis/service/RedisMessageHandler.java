package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.servise.Extractor;
import by.ivam.fellowtravelerbot.servise.FindPassengerRequestService;
import by.ivam.fellowtravelerbot.servise.handler.FindPassengerHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j
@Data
@AllArgsConstructor
@NoArgsConstructor

public class RedisMessageHandler {

    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    FindPassengerRequestService findPassengerRequestService;

    @Autowired
    private FindPassengerHandler findPassengerHandler;
    private final String FIND_PASSENGER_REQUEST = "find_passenger_request";

    public void handleMessage(String event, String message) {
        String requestType = Extractor.extractParameter(message, Extractor.INDEX_ZERO);
        int requestId = Extractor.extractId(message, Extractor.INDEX_ONE);
        String requestIdString = Extractor.extractParameter(message, Extractor.INDEX_ONE);
        switch (event) {
            case "hset" -> {
                log.debug("get new request type: " + requestType + ", id: " + requestIdString);
                findPassengerRequestService.findById(requestId);
            }
            case "expired" -> {
                log.debug("get expired request type: " + requestType + ", id: " + requestId);
                if (requestType.equals(FIND_PASSENGER_REQUEST)) {
                    findPassengerRequestService.disActivateRequestById(requestId);
                    findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
                }
            }
        }
    }
}
