package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.model.FindRideRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FindRideRequestService {

    FindRideRequest findById(int id);

    FindRideRequest findLastUserRequest(long chatId);

    Optional<FindRideRequest> findLastUserRequestOptional(long chatId);

    FindRideRequest addNewRequest(FindRideRequestDTO dto);

    FindRideRequest updateRequest(FindRideRequest request);

    List<FindRideRequest> usersActiveRequestList(long chatId);

    List<FindRideRequest> requestListByIdList(List<Integer> requestIdList);

    FindRideRequest cancelRequestById(int id);

    FindRideRequest disActivateRequestById(int requestId);

    void disActivateExpiredRequests(LocalDateTime presentTime);
}
