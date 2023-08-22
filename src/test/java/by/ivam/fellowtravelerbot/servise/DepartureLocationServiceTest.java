package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.repository.DepartureLocationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class DepartureLocationServiceTest {

    DepartureLocationService service = new DepartureLocationServiceImplementation();
    @Autowired
    DepartureLocationRepository repository;

    @Test
    void locationsList(){
        List<DepartureLocation> settlements = repository.findAllBySettlement_Id(2);
        assertNotNull(settlements);
    }

}