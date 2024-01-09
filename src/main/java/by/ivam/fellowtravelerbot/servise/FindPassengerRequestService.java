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

    List<FindPassengerRequest> requestListByIdList(List<Integer> requestsIdList);

    List<FindPassengerRequest> usersActiveRequestList(long chatId);

    FindPassengerRequest cancelRequestById(int requestId);

    FindPassengerRequest disActivateExpiredRequestById(int requestId);

    void disActivateExpiredRequestsOnStart(LocalDateTime presentTime);
    void removeFromRedis(int requestId);
}
