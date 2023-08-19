package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.CarDTO;
import by.ivam.fellowtravelerbot.model.Car;

import java.util.List;

public interface CarService {

    Car findById(int id);

    Car addNewCar(CarDTO carDTO, long chatId);

    List<Car> usersCarList(long chatId);

    void deleteCarById(int id);

    void deleteAllUsersCars(List<Integer> carIdList);
}
