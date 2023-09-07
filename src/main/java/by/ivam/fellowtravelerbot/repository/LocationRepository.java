package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    List<Location> findAllBySettlement_Id(int id);

}