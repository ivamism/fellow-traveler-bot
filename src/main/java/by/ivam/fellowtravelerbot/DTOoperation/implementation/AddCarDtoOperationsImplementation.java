package by.ivam.fellowtravelerbot.DTOoperation.implementation;

import by.ivam.fellowtravelerbot.DTO.CarDTO;
import by.ivam.fellowtravelerbot.DTOoperation.interfaces.AddCarDtoOperations;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
@Data
@Log4j
public class AddCarDtoOperationsImplementation implements AddCarDtoOperations {
//
//    @Autowired
//    Storages storage;
    private HashMap<Long, CarDTO> carDTOStorage = new HashMap<>();

    @Override
    public void addCarDTO(Long chatId, CarDTO carDTO) {
        carDTOStorage.put(chatId, carDTO);
//        storage.CarDTOStorage.put(chatId, carDTO);
        log.debug("add carDTO to storage " + carDTO + " with userId " + chatId);
    }

    @Override
    public void deleteCarDTO(Long chatId) {
        carDTOStorage.remove(chatId);
//        storage.CarDTOStorage.remove(chatId);
        log.debug("remove carDTO from storage");
    }

    @Override
    public CarDTO findCarDTO(Long chatId) {
        CarDTO carDTO = Optional.ofNullable(carDTOStorage.get(chatId)).orElseThrow();
//        CarDTO carDTO = Optional.ofNullable(storage.CarDTOStorage.get(chatId)).orElseThrow();
        log.debug("get carDTO from storage " + carDTO);
        return carDTO;
    }

    @Override
    public void setModel(Long chatId, String model) {
        findCarDTO(chatId).setModel(model);
        log.debug("set to carDTO model " + model);
    }

    @Override
    public void setColor(Long chatId, String color) {
        findCarDTO(chatId).setColor(color);
        log.debug("set to carDTO color " + color);
    }

    @Override
    public void setPlateNumber(Long chatId, String plateNumber) {
        findCarDTO(chatId).setPlateNumber(plateNumber);
        log.debug("set to carDTO color " + plateNumber);
    }

    @Override
    public void setCommentary(Long chatId, String commentary) {
        findCarDTO(chatId).setCommentary(commentary);
        log.debug("set to carDTO color " + commentary);
    }
}
