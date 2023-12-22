package by.ivam.fellowtravelerbot.stateful.interfaces;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;

public interface FindPassengerDtoOperations {
    FindPassengerRequestDTO getDTO(long chatId);

    void addFindPassengerDTO(long chatId, FindPassengerRequestDTO findPassengerRequestDTO);

    void update(long chatId, FindPassengerRequestDTO findPassengerRequestDTO);

    void delete(long chatId);
}
