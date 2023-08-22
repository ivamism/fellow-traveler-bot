package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CarServiceImplementationTest {

    @Autowired
    CarServiceImplementation carServiceImplementation;

    @Autowired
    CarRepository repository;

    @Test
    void usersCarList() {
        long chatId = 785703113;
        List<Car> cars = repository.findAllByUser_chatId(chatId);
        assertNotNull(cars);

    }

}