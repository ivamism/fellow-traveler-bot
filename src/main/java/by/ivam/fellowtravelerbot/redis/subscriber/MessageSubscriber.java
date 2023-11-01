package by.ivam.fellowtravelerbot.redis.subscriber;

import by.ivam.fellowtravelerbot.servise.FindPassengerRequestService;
import by.ivam.fellowtravelerbot.servise.handler.BaseHandler;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
@Log4j
@Service
public class MessageSubscriber implements MessageListener {

    @Autowired
    BaseHandler baseHandler;
    @Autowired
    FindPassengerRequestService requestService;

    public void onMessage(Message message, byte[] pattern) {
        String receivedMessage = message.toString();
        log.info("Message received: " + receivedMessage);
        System.out.println("Message received: " + receivedMessage);

        String channelTopic = baseHandler.extractProcess(receivedMessage);
        int requestId = baseHandler.extractId(receivedMessage, baseHandler.getFIRST_VALUE());
        switch (channelTopic){
            case "find_passenger_request" ->{
               requestService.cancelRequestById(requestId);
            }
        }
    }
}