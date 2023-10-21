package by.ivam.fellowtravelerbot.DTOoperation.implementation;

import by.ivam.fellowtravelerbot.DTO.LocationDTO;
import by.ivam.fellowtravelerbot.DTOoperation.interfaces.DepartureLocationDtoOperation;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
@Data
@Log4j
public class LocationDtoOperationImplementation implements DepartureLocationDtoOperation {
//    @Autowired
//    Storages storage;

    private HashMap<Long, LocationDTO> locationDTOStorage = new HashMap<>();


    @Override
    public void addLocation(long chatId, LocationDTO locationDTO) {
        locationDTOStorage.put(chatId, locationDTO);
//        storage.locationDTOStorage.put(chatId, locationDTO);
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
        locationDTOStorage.remove(chatId);
//        storage.locationDTOStorage.remove(chatId);
        log.debug("remove locationDTO from storage");
    }

    @Override
    public LocationDTO findDTO(long chatId) {
        LocationDTO locationDTO = Optional.ofNullable(locationDTOStorage.get(chatId)).orElseThrow();
//        LocationDTO locationDTO = Optional.ofNullable(storage.locationDTOStorage.get(chatId)).orElseThrow();
        log.debug("get locationDTO from storage " + locationDTO);
        return locationDTO;
    }
}
