package by.ivam.fellowtravelerbot.storages.implementation;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.storages.Storages;
import by.ivam.fellowtravelerbot.storages.interfaces.FindPassengerStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Data
@Log4j
public class FindPassengerStorageAccessImplementation implements FindPassengerStorageAccess {
    @Autowired
    Storages storage;

    @Override
    public void addPickUpPassengerDTO(long chatId, FindPassengerRequestDTO findPassengerRequestDTO) {
        storage.getPickUpPassengerRequestDTOStorage().put(chatId, findPassengerRequestDTO);
        log.debug("add carDTO to storage " + findPassengerRequestDTO + " with userId " + chatId);
    }

    @Override
    public void setDirection(long chatId, String direction) {
        storage.getPickUpPassengerRequestDTOStorage().get(chatId).setDirection(direction);
        log.debug("set direction: " + direction + " with userId " + chatId);
    }

    @Override
    public void setSettlement(long chatId, Settlement settlement) {
        storage.getPickUpPassengerRequestDTOStorage().get(chatId).setDepartureSettlement(settlement);
        log.debug("set settlement: " + settlement + " with userId " + chatId);
    }

    @Override
    public void setDepartureLocation(long chatId, DepartureLocation departureLocation) {
        storage.getPickUpPassengerRequestDTOStorage().get(chatId).setDepartureLocation(departureLocation);
        log.debug("set departureLocation: " + departureLocation + " with userId " + chatId);
    }

    @Override
    public void setDate(long chatId, LocalDate departureDate) {

    }

    @Override
    public void setTime(long chatId, LocalTime departureTime) {

    }
}
