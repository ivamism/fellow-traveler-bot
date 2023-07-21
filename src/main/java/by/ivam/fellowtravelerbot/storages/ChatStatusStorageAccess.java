package by.ivam.fellowtravelerbot.storages;

public interface ChatStatusStorageAccess {

    void addChatStatus(Long chatId, String chatStatus);
    void deleteChatStatus(Long chatId);
    String findChatStatus(Long chatId);

}
