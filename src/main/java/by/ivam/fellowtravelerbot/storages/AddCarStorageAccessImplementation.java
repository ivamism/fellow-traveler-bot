package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.DTO.CarDTO;
import by.ivam.fellowtravelerbot.storages.interfaces.AddCarStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Data
@Log4j
public class AddCarStorageAccessImplementation implements AddCarStorageAccess {

    @Autowired
    Storages storage;

    @Override
    public void addCarDTO(Long chatId, CarDTO carDTO) {
        storage.addCarStorage.put(chatId, carDTO);
        log.debug("add carDTO to storage " + carDTO + " with userId " + chatId);
    }

    @Override
    public void deleteCarDTO(Long chatId) {
        storage.addCarStorage.remove(chatId);
        log.debug("remove carDTO from storage");
    }

    @Override
    public CarDTO findCarDTO(Long chatId) {
        CarDTO carDTO = Optional.ofNullable(storage.addCarStorage.get(chatId)).orElseThrow();
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
