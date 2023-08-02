package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.bot.enums.ChatStatus;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j
public class ChatStatusStorageAccessImplementation implements ChatStatusStorageAccess {
    @Autowired
    Storages storage;

    @Override
    public void addChatStatus(Long chatId, String chatStatus) {
        storage.chatsStatusStorage.put(chatId, chatStatus);
        log.debug("add messageId " + chatId + " and status: " + chatStatus + " to storage which support step by step processes");
    }

    @Override
    public void deleteChatStatus(Long chatId) {
        storage.chatsStatusStorage.remove(chatId);
        log.debug("Remove chatStatus from storage");
    }

    @Override
    public String findChatStatus(Long chatId) {
        return storage.chatsStatusStorage.getOrDefault(chatId, String.valueOf(ChatStatus.NO_STATUS));
    }

}
