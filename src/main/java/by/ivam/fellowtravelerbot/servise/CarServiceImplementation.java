package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.repository.CarRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Log4j
public class CarServiceImplementation implements CarService {
    @Autowired
    private CarRepository carRepository;
    @Override
    public Car findById(int id) {
        return null;
    }

    @Override
    public List<Car> findAll() {
        return null;
    }

    @Override
    public void addNewCar(Car car) {
        carRepository.save(car);
        log.info("Car " + car + " saved to DB");

    }

    @Override
    public void deleteCar(int id) {

    }
}
