package by.ivam.fellowtravelerbot.redis.subscriber;

import by.ivam.fellowtravelerbot.redis.service.RedisMessageHandler;
import by.ivam.fellowtravelerbot.servise.Extractor;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;


@Log4j
@Data
public class MessageListener implements org.springframework.data.redis.connection.MessageListener {
    @Autowired
    RedisMessageHandler messageHandler;

    public void onMessage(Message message, byte[] pattern) {
        String event = Extractor.extractParameter(new String(message.getChannel()), Extractor.INDEX_ONE);
        String receivedMessage = message.toString();
        messageHandler.handleMessage(event, receivedMessage);
    }
}

