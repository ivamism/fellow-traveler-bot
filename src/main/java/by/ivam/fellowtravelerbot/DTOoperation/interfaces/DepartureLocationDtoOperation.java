package by.ivam.fellowtravelerbot.DTOoperation.interfaces;

import by.ivam.fellowtravelerbot.DTO.LocationDTO;

public interface DepartureLocationDtoOperation {
    void addLocation (long chatId, LocationDTO locationDTO);
    void setName(long chatId, String name);
    void setSettlement(long chatId, String settlement);
    void deleteLocation(long chatId);
    LocationDTO findDTO (long chatId);
}
