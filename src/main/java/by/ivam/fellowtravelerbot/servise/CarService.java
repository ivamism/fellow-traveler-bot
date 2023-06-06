package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;

import java.util.List;

public interface CarService {

    Car findById(int id);
    List<Car> findAll();

    void addNewCar(Car car);

    void deleteCar(int id);
}
