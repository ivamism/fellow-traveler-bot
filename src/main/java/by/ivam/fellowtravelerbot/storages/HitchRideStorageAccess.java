package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.DTO.HitchRideRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public interface HitchRideStorageAccess {
    void addHitchRideDTO(long chatId, HitchRideRequestDTO hitchRideRequestDTO);
void setSettlement(long chatId, int id);
void setDepartureLocation(long chatId, int id);
void setDate(long chatId, LocalDate departureDate);
void setTime(long chatId, LocalTime departureTime);


}
