package by.ivam.fellowtravelerbot.storages.interfaces;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.Location;
import by.ivam.fellowtravelerbot.model.Settlement;

import java.time.LocalDate;
import java.time.LocalTime;

public interface FindPassengerStorageAccess {
FindPassengerRequestDTO getDTO (long chatId);
    void addFindPassengerDTO(long chatId, FindPassengerRequestDTO findPassengerRequestDTO);
    void update (long chatId, FindPassengerRequestDTO findPassengerRequestDTO);
    void delete (long chatId);
    void setDirection (long chatId, String direction);

    void setDepartureSettlement(long chatId, Settlement settlement);
    void setDestinationSettlement(long chatId, Settlement settlement);

    void setDepartureLocation(long chatId, Location location);
    void setDestinationLocation(long chatId, Location location);

    void setDate(long chatId, LocalDate departureDate);

    void setTime(long chatId, LocalTime departureTime);
    void setCommentary(long chatId, String commentary);

}
