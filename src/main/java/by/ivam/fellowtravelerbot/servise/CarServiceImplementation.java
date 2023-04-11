package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CarServiceImplementation implements CarService {
    @Autowired
    private CarRepository carRepository;
    @Override
    public Car findById() {
        return null;
    }

    @Override
    public List<Car> findAll() {
        return null;
    }

    @Override
    public void addNewCar() {

    }

    @Override
    public void deleteCar() {

    }
}
