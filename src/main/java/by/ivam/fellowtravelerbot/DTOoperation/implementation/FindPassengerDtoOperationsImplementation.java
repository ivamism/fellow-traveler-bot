package by.ivam.fellowtravelerbot.DTOoperation.implementation;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.DTOoperation.interfaces.FindPassengerDtoOperations;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
@Log4j
public class FindPassengerDtoOperationsImplementation implements FindPassengerDtoOperations {

    private HashMap<Long, FindPassengerRequestDTO> findPassengerRequestDTOStorage = new HashMap<>();

    @Override
    public FindPassengerRequestDTO getDTO(long chatId) {
        return findPassengerRequestDTOStorage.get(chatId);
    }

    @Override
    public void addFindPassengerDTO(long chatId, FindPassengerRequestDTO findPassengerRequestDTO) {
        findPassengerRequestDTOStorage.put(chatId, findPassengerRequestDTO);
        log.debug("add FindPassengerRequestDTO to storage " + findPassengerRequestDTO + " with userId " + chatId);
    }

    @Override
    public void update(long chatId, FindPassengerRequestDTO findPassengerRequestDTO) {
        findPassengerRequestDTOStorage.replace(chatId, findPassengerRequestDTO);
        log.debug("update FindPassengerRequestDTO in storage " + findPassengerRequestDTO + " with userId " + chatId);
    }

    @Override
    public void delete(long chatId) {
        findPassengerRequestDTOStorage.remove(chatId);
        log.debug("delete FindPassengerRequestDTO from storage. chatId: " + chatId);
    }
}
