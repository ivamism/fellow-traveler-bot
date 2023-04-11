package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;

import java.util.List;

public interface CarService {

    Car findById();
    List<Car> findAll();

    void addNewCar();

    void deleteCar();
}
