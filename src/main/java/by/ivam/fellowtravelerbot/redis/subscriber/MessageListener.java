package by.ivam.fellowtravelerbot.redis.subscriber;

import by.ivam.fellowtravelerbot.Extractor;
import by.ivam.fellowtravelerbot.servise.FindPassengerRequestServiceImplementation;
import by.ivam.fellowtravelerbot.servise.FindRideRequestService;
import by.ivam.fellowtravelerbot.servise.MatchService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;

@Data
@Log4j

public class MessageListener implements org.springframework.data.redis.connection.MessageListener {
    private final String FIND_PASSENGER_REQUEST = "find_passenger_request";
    //    @Autowired
//    BaseHandler baseHandler = new BaseHandler();
    @Autowired
    FindPassengerRequestServiceImplementation findPassengerRequestService;

    @Autowired
    FindRideRequestService findRideRequestService;

    @Autowired
    MatchService matchService;

    public void onMessage(Message message, byte[] pattern) {

        String s = new String(message.getChannel());
        String event = Extractor.extractParameter(new String(message.getChannel()), Extractor.INDEX_ONE);
        String receivedMessage = message.toString();
        log.info("Message received: " + receivedMessage);
        String requestType = Extractor.extractParameter(receivedMessage, Extractor.INDEX_ZERO);
        int requestId = Extractor.extractId(receivedMessage, Extractor.INDEX_ONE);
        switch (event) {
            case "hset" -> {
                log.debug("get new request type: " + requestType+ ", id: " + requestId);

//                findPassengerRequestService.findById(requestId);
            }
            case "expired" -> {
                log.debug("get expired request type: " + requestType+ ", id: " + requestId);
                if(requestType.equals(FIND_PASSENGER_REQUEST)) {
                    findPassengerRequestService.disActivateRequestById(requestId);
                }
            }
        }


    }
}