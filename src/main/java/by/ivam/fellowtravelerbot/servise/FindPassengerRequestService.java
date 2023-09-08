package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;

import java.util.List;
import java.util.Optional;

public interface FindPassengerRequestService {

    FindPassengerRequest findById(int id);
    FindPassengerRequest findLastUserRequest (long chatId);

    FindPassengerRequest addNewRequest(FindPassengerRequestDTO dto);
    FindPassengerRequest updateRequest(FindPassengerRequest request);

    List<FindPassengerRequest> usersRequestList(long chatId);
    List<FindPassengerRequest> usersActiveRequestList(long chatId);

    void cancelRequestById(int id);

    void cancelAllUsersActiveRequests(List<Integer> requestsIdList);
}
