package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.handler.enums.ChatStatus;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j
public class StorageAccessImplementation implements StorageAccess {
    @Autowired
    Storages storage;

    @Override
    public void addChatStatus(Long chatId, String chatStatus) {
        storage.activeChatsStorage.put(chatId, chatStatus);
        log.debug("add messageId " + chatId + " and status: " + chatStatus + " to storage which support step by step processes");
    }

    @Override
    public void deleteChatStatus(Long chatId) {
        storage.activeChatsStorage.remove(chatId);
        log.debug("Remove botStatus from storage");
    }

    @Override
    public String findChatStatus(Long chatId) {
        return storage.activeChatsStorage.getOrDefault(chatId, String.valueOf(ChatStatus.NO_STATUS));
    }

    @Override
    public void addUserFirstName(Long chatId, String UserFirstName) {
        storage.userNamesStorage.put(chatId, UserFirstName);
        log.debug("add edited UserFirstName to storage");
    }

    @Override
    public void deleteUserFirstName(Long chatId) {
        storage.userNamesStorage.remove(chatId);
        log.debug("Remove UserFirstName from storage");
    }

    @Override
    public String findUserFirstName(Long chatId) {
        return storage.userNamesStorage.getOrDefault(chatId, "Имя этого пользователя отсутствует в хранилище");
    }

}
