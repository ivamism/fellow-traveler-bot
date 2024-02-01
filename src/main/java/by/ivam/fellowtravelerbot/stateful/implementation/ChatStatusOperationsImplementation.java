package by.ivam.fellowtravelerbot.stateful.implementation;

import by.ivam.fellowtravelerbot.bot.enums.ChatStatus;
import by.ivam.fellowtravelerbot.stateful.interfaces.ChatStatusOperations;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
@Log4j2
public class ChatStatusOperationsImplementation implements ChatStatusOperations {
    private HashMap<Long, String> chatsStatusStorage = new HashMap<>();

    @Override
    public void addChatStatus(Long chatId, String chatStatus) {
        chatsStatusStorage.put(chatId, chatStatus);
        log.debug("add messageId " + chatId + " and status: " + chatStatus + " to storage which support step by step processes");
    }

    @Override
    public void deleteChatStatus(Long chatId) {
        chatsStatusStorage.remove(chatId);
        log.debug("Remove chatStatus from storage");
    }

    @Override
    public String findChatStatus(Long chatId) {
        return chatsStatusStorage.getOrDefault(chatId, String.valueOf(ChatStatus.NO_STATUS));
    }
}
