package by.ivam.fellowtravelerbot.DTOoperation.interfaces;

public interface ChatStatusOperations {

    void addChatStatus(Long chatId, String chatStatus);
    void deleteChatStatus(Long chatId);
    String findChatStatus(Long chatId);

}
