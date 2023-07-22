package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;
import by.ivam.fellowtravelerbot.storages.interfaces.DepartureLocationStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Data
@Log4j
public class DepartureLocationStorageAccessImplementation implements DepartureLocationStorageAccess {
    @Autowired
    Storages storage;

    @Override
    public void addLocation(long chatId, DepartureLocationDTO locationDTO) {
        storage.departureLocationDTOStorage.put(chatId, locationDTO);
        log.debug("put location " + locationDTO + ", chatId: " + chatId + " to storage");
    }

    @Override
    public void setName(long chatId, String name) {
        findDTO(chatId).setName(name);
    }

    @Override
    public void setSettlement(long chatId, String settlement) {

    }

    @Override
    public void deleteLocation(long chatId) {
        storage.departureLocationDTOStorage.remove(chatId);
        log.debug("remove locationDTO from storage");
    }

    @Override
    public DepartureLocationDTO findDTO(long chatId) {
        DepartureLocationDTO locationDTO = Optional.ofNullable(storage.departureLocationDTOStorage.get(chatId)).orElseThrow();
        log.debug("get locationDTO from storage " + locationDTO);
        return locationDTO;
    }
}
