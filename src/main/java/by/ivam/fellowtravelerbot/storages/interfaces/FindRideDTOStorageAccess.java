package by.ivam.fellowtravelerbot.storages.interfaces;

import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public interface FindRideDTOStorageAccess {
    void addFindRideDTO(long chatId, FindRideRequestDTO findRideRequestDTO);

    void setSettlement(long chatId, int id);

    void setDepartureLocation(long chatId, int id);

    void setDate(long chatId, LocalDate departureDate);

    void setTime(long chatId, LocalTime departureTime);


}
