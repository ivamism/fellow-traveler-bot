package by.ivam.fellowtravelerbot.DTO.stateOperations.interfaces;

import by.ivam.fellowtravelerbot.DTO.UserDTO;
import by.ivam.fellowtravelerbot.model.Settlement;

public interface UserDtoOperations {
    void addUserDTO(Long chatId, UserDTO userDTO);
    void deleteUserDTO(Long chatId);
    UserDTO findUserDTO(Long chatId);
    void setFirstName(Long chatId, String firstName);
    void setResidence(Long chatId, Settlement residence);
}
