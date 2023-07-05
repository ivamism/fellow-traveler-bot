package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;

import java.util.List;

public interface CarService {

    Car findById(int id);

    Car addNewCar(Car car);

    List<Car> usersCarList(long chatId);

    void deleteCarById(int id);

    void deleteCar(Car car);

    void deleteAllUsersCars(long chatId);
    Car updateCar (Car car);
}
