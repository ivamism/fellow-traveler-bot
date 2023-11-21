package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FindPassengerRequestService {

    FindPassengerRequest findById(int id);

    FindPassengerRequest findLastUserRequest(long chatId);

    Optional<FindPassengerRequest> findLastUserRequestOptional(long chatId);

    FindPassengerRequest addNewRequest(FindPassengerRequestDTO dto);

    FindPassengerRequest updateRequest(FindPassengerRequest request);

    List<FindPassengerRequest> usersRequestList(long chatId);
    List<FindPassengerRequest> requestListByIdList(List<Integer> requestsIdList);

    List<FindPassengerRequest> usersActiveRequestList(long chatId);

    FindPassengerRequest cancelRequestById(int requestId);
    FindPassengerRequest disActivateRequestById(int requestId);
    void disActivateExpiredRequests(LocalDateTime presentTime);

    void cancelAllUsersActiveRequests(List<Integer> requestsIdList);
}
