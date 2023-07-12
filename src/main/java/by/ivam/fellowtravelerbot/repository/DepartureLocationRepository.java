package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.DepartureLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartureLocationRepository extends JpaRepository<DepartureLocation, Integer> {
}