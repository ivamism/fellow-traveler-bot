package by.ivam.fellowtravelerbot.storages.implementation;

import by.ivam.fellowtravelerbot.DTO.PickUpPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.model.Direction;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.storages.Storages;
import by.ivam.fellowtravelerbot.storages.interfaces.PickUpPassengerStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Data
@Log4j
public class PickUpStorageAccessImplementation implements PickUpPassengerStorageAccess {
    @Autowired
    Storages storage;

    @Override
    public void addPickUpPassengerDTO(long chatId, PickUpPassengerRequestDTO pickUpPassengerRequestDTO) {
        storage.getPickUpPassengerRequestDTOStorage().put(chatId, pickUpPassengerRequestDTO);
        log.debug("add carDTO to storage " + pickUpPassengerRequestDTO + " with userId " + chatId);
    }

    @Override
    public void setDirection(long chatId, Direction direction) {
        storage.getPickUpPassengerRequestDTOStorage().get(chatId).setDirection(direction);
        log.debug("set direction: " + direction + " with userId " + chatId);
    }

    @Override
    public void setSettlement(long chatId, Settlement settlement) {
        storage.getPickUpPassengerRequestDTOStorage().get(chatId).setSettlement(settlement);
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
