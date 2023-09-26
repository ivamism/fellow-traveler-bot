package by.ivam.fellowtravelerbot.storages.implementation;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.storages.Storages;
import by.ivam.fellowtravelerbot.storages.interfaces.FindRideDTOStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Data
@Log4j
public class FindRideDTOStorageAccessImplementation implements FindRideDTOStorageAccess {
    @Autowired
    Storages storage;

    @Override
    public void addFindRideDTO(long chatId, FindRideRequestDTO findRideRequestDTO) {
        storage.getFindRideRequestDTOStorage().put(chatId, findRideRequestDTO);
        log.debug("add FindPassengerRequestDTO to storage " + findRideRequestDTO + " with userId " + chatId);
    }

    @Override
    public FindRideRequestDTO getDTO(long chatId) {
        log.debug("getDTO");
        return storage.getFindRideRequestDTOStorage().get(chatId);
    }

    @Override
    public void update(long chatId, FindRideRequestDTO requestDTO) {
        storage.getFindRideRequestDTOStorage().put(chatId, requestDTO);
        log.debug("update FindRideRequestDTO in storage " + requestDTO + " with userId " + chatId);

    }

    @Override
    public void delete(long chatId) {
        storage.getFindRideRequestDTOStorage().remove(chatId);
        log.debug("delete FindRideRequestDTO from storage. chatId: " + chatId);
    }


}
