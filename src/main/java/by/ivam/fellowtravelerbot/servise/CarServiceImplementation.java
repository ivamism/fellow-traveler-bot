package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.CarDTO;
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
    @Autowired
    UserService userService;

    @Override
    public Car findById(int id) {
        return Optional.ofNullable(carRepository.findById(id).get()).orElseThrow();
    }

    @Override
    public Car addNewCar(CarDTO carDTO, long chatId) {
        Car car = new Car();
        car.setModel(carDTO.getModel())
                .setColor(carDTO.getColor())
                .setPlateNumber(carDTO.getPlateNumber())
                .setCommentary(carDTO.getCommentary())
                .setUser(userService.findUserById(chatId));
        carRepository.save(car);
        log.info("CarService: Car " + carDTO + " saved to DB");
        return car;
    }

    @Override
    public List<Car> usersCarList(long chatId) {
log.info("CarService: get User's cars list");
        return carRepository.findAllByUser_chatId(chatId);
    }

    @Override
    public void deleteCarById(int id) {
        carRepository.deleteById(id);
        log.info("CarService: delete car - carId: " + id);
    }

    @Override
    public void deleteAllUsersCars(List<Integer> carIdList) {
        carIdList.forEach(id -> deleteCarById(id));

//        carRepository.deleteAllByUser_chatId(chatId);
        log.info("CarService: deleteAllUsersCars");
    }

}
