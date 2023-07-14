package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.repository.DepartureLocationRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class DepartureLocationServiceImplementation implements DepartureLocationService {

    @Autowired
    DepartureLocationRepository departureLocationRepository;

    @Override
    public DepartureLocation findById(int id) {
        DepartureLocation location = Optional.ofNullable(departureLocationRepository.findById(id).get()).orElseThrow();
        log.info("Get location by id" + location);
        return location;
    }

    @Override
    public List<DepartureLocation> findAll() {
        return null;
    }

    @Override
    public DepartureLocation addNewLocation(DepartureLocationDTO locationDTO) {
        DepartureLocation location = new DepartureLocation();
        location.setName(locationDTO.getName())
                .setSettlement(locationDTO.getSettlement());
        departureLocationRepository.save(location);
        return location;
    }

    @Override
    public DepartureLocation updateLocation(int id) {
        return null;
    }

    @Override
    public void deleteById(int id) {

    }
}
