package by.ivam.fellowtravelerbot.handler.storages;

public interface StorageAccess {

    void addChatStatus(Long chatId, String chatStatus);
    void deleteChatStatus(Long chatId);
    String findChatStatus(Long chatId);

    void addUserFirstName(Long chatId, String chatStatus);
    void deleteUserFirstName(Long chatId);
    String findUserFirstName(Long chatId);

}
