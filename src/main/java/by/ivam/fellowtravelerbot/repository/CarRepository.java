package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Integer> {

    List<Car> findAllByUser_chatId(Long chatId);

}