package by.ivam.fellowtravelerbot.storages.implementation;

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
        storage.getFindRideRequestDTOStorage().put(chatId,findRideRequestDTO);
    }

    @Override
    public void setSettlement(long chatId, int id) {

    }

    @Override
    public void setDepartureLocation(long chatId, int id) {

    }

    @Override
    public void setDate(long chatId, LocalDate departureDate) {

    }

    @Override
    public void setTime(long chatId, LocalTime departureTime) {

    }
}
