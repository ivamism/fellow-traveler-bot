package by.ivam.fellowtravelerbot.stateful.interfaces;

import by.ivam.fellowtravelerbot.DTO.CarDTO;

public interface AddCarDtoOperations {
    void addCarDTO(Long chatId, CarDTO carDTO);
    void deleteCarDTO(Long chatId);
    CarDTO findCarDTO(Long chatId);
    void setModel (Long chatId, String model);
    void setColor (Long chatId, String color);
    void setPlateNumber (Long chatId, String plateNumber);
    void setCommentary(Long chatId, String commentary);

}
