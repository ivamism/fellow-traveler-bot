package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;

public interface DepartureLocationStorageAccess {
    void addLocation (long chatId, DepartureLocationDTO locationDTO);
    void setName(long chatId, String name);
    void setSettlement(long chatId, String settlement);
    void deleteLocation(long chatId);
    DepartureLocationDTO findDTO (long chatId);
}
