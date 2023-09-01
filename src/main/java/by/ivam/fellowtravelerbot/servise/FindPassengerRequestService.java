package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;

import java.util.List;

public interface FindPassengerRequestService {

    FindPassengerRequest findById(int id);

    FindPassengerRequest addNewRequest(FindPassengerRequestDTO dto);
    FindPassengerRequest updateRequest(FindPassengerRequest request);

    List<FindPassengerRequest> usersRequestList(long chatId);
    List<FindPassengerRequest> usersActivRequestList(long chatId);

    void deleteCarById(int id);

    void deleteAllUsersCars(List<Integer> carIdList);
}
