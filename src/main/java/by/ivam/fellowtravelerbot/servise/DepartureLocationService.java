package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.LocationDTO;
import by.ivam.fellowtravelerbot.model.Location;

import java.util.List;

public interface DepartureLocationService {
    Location findById(int id);
    List<Location> findAll();
    List<Location> findAllBySettlement(int settlementId);
    Location addNewLocation(LocationDTO locationDTO);
    Location updateLocation(int id);
    void deleteById(int id);
}
