package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {

    List<Car> findAllByUser_chatId(Long chatId);

}