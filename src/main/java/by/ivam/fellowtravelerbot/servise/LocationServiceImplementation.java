package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.LocationDTO;
import by.ivam.fellowtravelerbot.model.Location;
import by.ivam.fellowtravelerbot.repository.LocationRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class LocationServiceImplementation implements LocationService {

    @Autowired
    LocationRepository departureLocationRepository;

    @Override
    public Location findById(int id) {
        Location location = Optional.ofNullable(departureLocationRepository.findById(id).get()).orElseThrow();
        log.info("Get location by id" + location);
        return location;
    }

    @Override
    public List<Location> findAll() {
        return null;
    }

    @Override
    public List<Location> findAllBySettlement(int settlementId) {
        return departureLocationRepository.findAllBySettlement_Id(settlementId);
    }

    @Override
    public Location addNewLocation(LocationDTO locationDTO) {
        Location location = new Location();
        location.setName(locationDTO.getName())
                .setSettlement(locationDTO.getSettlement());
        departureLocationRepository.save(location);
        return location;
    }

    @Override
    public Location updateLocation(int id) {
        return null;
    }

    @Override
    public void deleteById(int id) {

    }
}
