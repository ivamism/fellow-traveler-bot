package by.ivam.fellowtravelerbot.storages.interfaces;

import by.ivam.fellowtravelerbot.DTO.PickUpPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.model.Direction;
import by.ivam.fellowtravelerbot.model.Settlement;

import java.time.LocalDate;
import java.time.LocalTime;

public interface PickUpPassengerStorageAccess {

    void addPickUpPassengerDTO(long chatId, PickUpPassengerRequestDTO pickUpPassengerRequestDTO);

    void setDirection (long chatId, Direction direction);

    void setSettlement(long chatId, Settlement settlement);

    void setDepartureLocation(long chatId, DepartureLocation departureLocation);

    void setDate(long chatId, LocalDate departureDate);

    void setTime(long chatId, LocalTime departureTime);
}
