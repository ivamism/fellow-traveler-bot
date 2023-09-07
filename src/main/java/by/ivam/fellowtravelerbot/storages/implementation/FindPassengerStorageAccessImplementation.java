package by.ivam.fellowtravelerbot.storages.implementation;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.Location;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.storages.Storages;
import by.ivam.fellowtravelerbot.storages.interfaces.FindPassengerStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Data
@Log4j
public class FindPassengerStorageAccessImplementation implements FindPassengerStorageAccess {
    @Autowired
    Storages storage;

    @Override
    public FindPassengerRequestDTO getDTO(long chatId) {
        return storage.getFindPassengerRequestDTOStorage().get(chatId);
    }

    @Override
    public void addPickUpPassengerDTO(long chatId, FindPassengerRequestDTO findPassengerRequestDTO) {
        storage.getFindPassengerRequestDTOStorage().put(chatId, findPassengerRequestDTO);
        log.debug("add FindPassengerRequestDTO to storage " + findPassengerRequestDTO + " with userId " + chatId);
    }

    @Override
    public void update(long chatId, FindPassengerRequestDTO findPassengerRequestDTO) {
        storage.getFindPassengerRequestDTOStorage().replace(chatId, findPassengerRequestDTO);
        log.debug("update FindPassengerRequestDTO in storage " + findPassengerRequestDTO + " with userId " + chatId);
    }

    @Override
    public void delete(long chatId) {
        storage.getFindPassengerRequestDTOStorage().remove(chatId);
        log.debug("delete FindPassengerRequestDTO from storage. chatId: " +   chatId);
    }

    @Override
    public void setDirection(long chatId, String direction) {
        storage.getFindPassengerRequestDTOStorage().get(chatId).setDirection(direction);
        log.debug("set direction: " + direction + " with userId " + chatId);
    }

    @Override
    public void setDepartureSettlement(long chatId, Settlement settlement) {
        storage.getFindPassengerRequestDTOStorage().get(chatId).setDepartureSettlement(settlement);
        log.debug("set settlement: " + settlement + " with userId " + chatId);
    }

    @Override
    public void setDestinationSettlement(long chatId, Settlement settlement) {
    }

    @Override
    public void setDepartureLocation(long chatId, Location location) {
        storage.getFindPassengerRequestDTOStorage().get(chatId).setDepartureLocation(location);
        log.debug("set location: " + location + " with userId " + chatId);
    }

    @Override
    public void setDestinationLocation(long chatId, Location location) {

    }

    @Override
    public void setDate(long chatId, LocalDate departureDate) {

    }

    @Override
    public void setTime(long chatId, LocalTime departureTime) {

    }

    @Override
    public void setCommentary(long chatId, String commentary) {

    }
}
