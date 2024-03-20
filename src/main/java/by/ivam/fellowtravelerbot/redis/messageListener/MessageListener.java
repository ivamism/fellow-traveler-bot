package by.ivam.fellowtravelerbot.redis.messageListener;

import by.ivam.fellowtravelerbot.redis.Events;
import by.ivam.fellowtravelerbot.redis.service.RedisMessageHandler;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;

import java.util.Arrays;
import java.util.Optional;

@Data
@Log4j
public class MessageListener implements org.springframework.data.redis.connection.MessageListener {

    @Autowired
    RedisMessageHandler messageHandler;

    public void onMessage(Message message, byte[] pattern) {
        String event = extractEvent(new String(message.getChannel()));
        String receivedMessage = message.toString();

        if (isMatchedEvent(event))
            messageHandler.handleEvent(event, receivedMessage);
        else log.debug("caught unhandled event, or event has wrong format: ".formatted(event));
    }

    private String extractEvent(String keyEvent) {
        String regex = ":";
        String emptyString = "_";
        int extractingSectionNumber = 1;
        return Optional.ofNullable(keyEvent.split(regex)[extractingSectionNumber]).orElse(emptyString);
    }

    private boolean isMatchedEvent(String caughtEvent) {
        return Arrays.stream(Events.values())
                .map(events -> events.getValue())
                .anyMatch(event -> event.equals(caughtEvent));
    }
}

