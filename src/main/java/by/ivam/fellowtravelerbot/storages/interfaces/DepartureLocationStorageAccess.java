package by.ivam.fellowtravelerbot.storages.interfaces;

import by.ivam.fellowtravelerbot.DTO.LocationDTO;

public interface DepartureLocationStorageAccess {
    void addLocation (long chatId, LocationDTO locationDTO);
    void setName(long chatId, String name);
    void setSettlement(long chatId, String settlement);
    void deleteLocation(long chatId);
    LocationDTO findDTO (long chatId);
}
