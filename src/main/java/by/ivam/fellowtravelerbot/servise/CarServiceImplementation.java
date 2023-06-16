package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.repository.CarRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
// TODO кастомизировать exception
@Service
@Log4j
public class CarServiceImplementation implements CarService {
    @Autowired
    private CarRepository carRepository;

    @Override
    public Car findById(int id) {
        return Optional.ofNullable(carRepository.findById(id).get()).orElseThrow();
    }


    @Override
    public Car addNewCar(Car car) {
        carRepository.save(car);
        log.info("Car " + car + " saved to DB");
        return car;
    }

    @Override
    public List<Car> usersCarList(long chatId) {

        return carRepository.findAllByUser_chatId(chatId);
    }

    @Override
    public void deleteCarById(int id) {
        carRepository.deleteById(id);
        log.info("delete car carId: " + id);
    }

    @Override
    public void deleteCar(Car car) {
        carRepository.delete(car);
        log.info("delete car: " + car);
    }

    @Override
    public void deleteAllUsersCars(long chatId) {
//        carRepository.deleteAllCarsByUserId(chatId);
        log.info("delete all cars belongs to User userId: " + chatId);
    }
}
