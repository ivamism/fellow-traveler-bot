package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.DepartureLocation;

import java.util.List;

public interface DepartureLocationService {
    DepartureLocation findById();
    List<DepartureLocation> findAll();
    DepartureLocation addNewLocation();
    DepartureLocation updateLocation();
    void deleteById();
}
