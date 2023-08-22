package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;
import by.ivam.fellowtravelerbot.model.DepartureLocation;

import java.util.List;

public interface DepartureLocationService {
    DepartureLocation findById(int id);
    List<DepartureLocation> findAll();
    List<DepartureLocation> findAllBySettlement(int settlementId);
    DepartureLocation addNewLocation(DepartureLocationDTO locationDTO);
    DepartureLocation updateLocation(int id);
    void deleteById(int id);
}
