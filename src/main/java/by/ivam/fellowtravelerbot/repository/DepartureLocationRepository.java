package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.DepartureLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartureLocationRepository extends JpaRepository<DepartureLocation, Integer> {
    List<DepartureLocation> findAllBySettlement_Id(int id);

}