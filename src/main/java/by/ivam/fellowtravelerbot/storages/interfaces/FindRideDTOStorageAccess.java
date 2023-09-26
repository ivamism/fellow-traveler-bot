package by.ivam.fellowtravelerbot.storages.interfaces;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public interface FindRideDTOStorageAccess {
    void addFindRideDTO(long chatId, FindRideRequestDTO findRideRequestDTO);
    FindRideRequestDTO getDTO(long chatId);


    void update(long chatId, FindRideRequestDTO findRideRequestDTO);

    void delete(long chatId);

//    void setSettlement(long chatId, int id);
//
//    void setDepartureLocation(long chatId, int id);
//
//    void setDate(long chatId, LocalDate departureDate);
//
//    void setTime(long chatId, LocalTime departureTime);


}
