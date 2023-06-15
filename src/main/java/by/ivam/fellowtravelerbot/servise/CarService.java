package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;

public interface CarService {

    Car findById(int id);
        Car addNewCar(Car car);

    void deleteCarById(int id);

    void deleteCar(Car car);
    void deleteAllUsersCars(long chatId);
}
