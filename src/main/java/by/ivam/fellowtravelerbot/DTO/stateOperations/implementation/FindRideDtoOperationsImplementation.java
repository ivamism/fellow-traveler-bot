package by.ivam.fellowtravelerbot.DTO.stateOperations.implementation;

import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.DTO.stateOperations.interfaces.FindRideDtoOperations;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
@Log4j
public class FindRideDtoOperationsImplementation implements FindRideDtoOperations {

    private HashMap<Long, FindRideRequestDTO> findRideRequestDTOStorage = new HashMap<>();

    @Override
    public void addFindRideDTO(long chatId, FindRideRequestDTO findRideRequestDTO) {
        findRideRequestDTOStorage.put(chatId, findRideRequestDTO);
        log.debug("add FindPassengerRequestDTO to storage " + findRideRequestDTO + " with userId " + chatId);
    }

    @Override
    public FindRideRequestDTO getDTO(long chatId) {
        log.debug("getDTO");
        return findRideRequestDTOStorage.get(chatId);
    }

    @Override
    public void update(long chatId, FindRideRequestDTO requestDTO) {
        findRideRequestDTOStorage.put(chatId, requestDTO);
        log.debug("update FindRideRequestDTO in storage " + requestDTO + " with userId " + chatId);
    }

    @Override
    public void delete(long chatId) {
        findRideRequestDTOStorage.remove(chatId);
        log.debug("delete FindRideRequestDTO from storage. chatId: " + chatId);
    }
}
