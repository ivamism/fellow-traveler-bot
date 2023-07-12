package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j
public class DepartureLocationStorageAccessImplementation implements DepartureLocationStorageAccess {
    @Override
    public void addLocation(long chatId, DepartureLocationDTO locationDTO) {

    }

    @Override
    public void setName(long chatId, String name) {

    }

    @Override
    public void setSettlement(long chatId, String settlement) {

    }

    @Override
    public void deleteLocation(long chatId) {

    }

    @Override
    public DepartureLocationDTO findDTO(long chatId) {
        return null;
    }
}
