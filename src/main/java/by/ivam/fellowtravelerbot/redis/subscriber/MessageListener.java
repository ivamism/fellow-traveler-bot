package by.ivam.fellowtravelerbot.redis.subscriber;

import by.ivam.fellowtravelerbot.Extractor;
import by.ivam.fellowtravelerbot.servise.FindPassengerRequestServiceImplementation;
import by.ivam.fellowtravelerbot.servise.handler.FindPassengerHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;

//@Service
@Log4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageListener implements org.springframework.data.redis.connection.MessageListener {
    private final String FIND_PASSENGER_REQUEST = "find_passenger_request";
    
    @Autowired
    Extractor extractor;

//    @Autowired
//    FindPassRequestRedisService findPassRequestRedisService;

    @Autowired
    private FindPassengerHandler findPassengerHandler;// = new FindPassengerHandler();
    @Autowired
    private FindPassengerRequestServiceImplementation findPassengerRequestService;// = new FindPassengerRequestServiceImplementation();

//
//    @Autowired
//    FindRideRequestService findRideRequestService;
//
//    @Autowired
//    MatchService matchService;

    public void onMessage(Message message, byte[] pattern) {

//        String s = new String(message.getChannel());
        String event = extractor.extractParameter(new String(message.getChannel()), extractor.getINDEX_ONE());
        String receivedMessage = message.toString();
        log.info("Message received: " + receivedMessage);
        String requestType = extractor.extractParameter(receivedMessage, extractor.getINDEX_ZERO());
        switch (event) {

            case "hset" -> {
                int requestId = extractor.extractId(receivedMessage, extractor.getINDEX_ONE());
                String requestIdString = extractor.extractParameter(receivedMessage, extractor.getINDEX_ONE());
                log.debug("get new request type: " + requestType + ", id: " + requestIdString);
                findPassengerRequestService.findById(requestId);
            }
            case "expired" -> {
                int requestId = extractor.extractId(receivedMessage, extractor.getINDEX_ONE());
                log.debug("get expired request type: " + requestType + ", id: " + requestId);
                if (requestType.equals(FIND_PASSENGER_REQUEST)) {
                    findPassengerRequestService.disActivateRequestById(requestId);
                    findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
                }
            }
        }


    }
}

