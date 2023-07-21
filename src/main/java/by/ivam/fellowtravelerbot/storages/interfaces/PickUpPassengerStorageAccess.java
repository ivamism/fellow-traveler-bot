package by.ivam.fellowtravelerbot.storages.interfaces;

import by.ivam.fellowtravelerbot.DTO.HitchRideRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public interface PickUpPassengerStorageAccess {

    void addPickUpPassengerDTO(long chatId, HitchRideRequestDTO hitchRideRequestDTO);

    void setSettlement(long chatId, int id);

    void setDepartureLocation(long chatId, int id);

    void setDate(long chatId, LocalDate departureDate);

    void setTime(long chatId, LocalTime departureTime);
}
