package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Integer> {
}