package by.ivam.fellowtravelerbot.handler.storage;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j
public class StorageAccessImplementation implements  StorageAccess{
    @Autowired
    BotStateStorage storage;
    @Override
    public void addChatStatus(Integer messageId, String chatStatus) {
        storage.activeChatsStorage.put(messageId, chatStatus);
        log.debug("add messageId " + messageId + " and status: " + chatStatus + " to storage which support step by step processes");
    }

    @Override
    public void deleteChatStatus(Integer messageId) {
        storage.activeChatsStorage.remove(messageId);
        log.debug("Remove botStatus from storage");
    }
    @Override
    public String findChatStatus(Integer messageId) {
        String chatStatus = storage.activeChatsStorage.getOrDefault(messageId, "NO_STATUS");

        return chatStatus;
    }
}
