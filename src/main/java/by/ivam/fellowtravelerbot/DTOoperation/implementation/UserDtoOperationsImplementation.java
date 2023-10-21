package by.ivam.fellowtravelerbot.DTOoperation.implementation;

import by.ivam.fellowtravelerbot.DTO.UserDTO;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.DTOoperation.interfaces.UserDtoOperations;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
@Data
@Log4j

public class UserDtoOperationsImplementation implements UserDtoOperations {
//    @Autowired
//    Storages storage;

    HashMap<Long, UserDTO> userDTOStorage = new HashMap<>();


    @Override
    public void addUserDTO(Long chatId, UserDTO userDTO) {
//        storage.getUserDTOStorage().put(chatId, userDTO);
        userDTOStorage.put(chatId, userDTO);
        log.debug("add to storage userDTO: " + userDTO);
    }

    @Override
    public void deleteUserDTO(Long chatId) {
        userDTOStorage.remove(chatId);
//        storage.getUserDTOStorage().remove(chatId);
        log.debug("delete from storage UserDTO - user chatId: " + chatId);

    }

    @Override
    public UserDTO findUserDTO(Long chatId) {
        UserDTO userDTO = Optional.ofNullable(userDTOStorage.get(chatId)).orElseThrow();
//        UserDTO userDTO = Optional.ofNullable(storage.getUserDTOStorage().get(chatId)).orElseThrow();
        log.debug("get from storage UserDTO: " + userDTO);
        return userDTO;
    }

    // TODO переделать два нижних метода на один update()
    @Override
    public void setFirstName(Long chatId, String firstName) {
        findUserDTO(chatId).setFirstName(firstName);
        log.debug("Set firstName to UserDTO - chatId: " + chatId + " firstname: " + firstName);
    }

    @Override
    public void setResidence(Long chatId, Settlement residence) {
        findUserDTO(chatId).setResidence(residence);
        log.debug("Set settlement residence to UserDTO - chatId: " + chatId + " residence: " + residence);
    }
}
