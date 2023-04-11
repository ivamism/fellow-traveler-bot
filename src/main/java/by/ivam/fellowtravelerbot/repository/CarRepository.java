package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}