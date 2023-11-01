package by.ivam.fellowtravelerbot.redis.subscriber;

import lombok.extern.log4j.Log4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Log4j
@Service
public class MessageSubscriber implements MessageListener {
    public static List<String> messageList = new ArrayList<String>();
    public void onMessage(Message message, byte[] pattern) {
        messageList.add(message.toString());
        log.info("Message received: " + message.toString());
        System.out.println("Message received: " + message.toString());
    }
}