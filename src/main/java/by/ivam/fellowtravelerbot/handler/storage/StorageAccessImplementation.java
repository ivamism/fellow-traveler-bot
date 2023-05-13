package by.ivam.fellowtravelerbot.handler.storage;

import by.ivam.fellowtravelerbot.handler.enums.BotStatus;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
@Data
public class StorageAccessImplementation implements  StorageAccess{
    @Autowired
    BotStateStorage storage;
    @Override
    public void addChatStatus(Integer messageId, String chatStatus) {

    }

    @Override
    public void deleteChatStatus(Integer messageId) {

    }

    @Override
    public String findChatStatus(Integer messageId) {
        String chatStatus = Optional.ofNullable(storage.getActiveChatsStorage().get(messageId)).orElse(String.valueOf(BotStatus.NO_STATUS));

        return chatStatus;
    }
}
