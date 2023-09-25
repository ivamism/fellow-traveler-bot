package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.repository.FindRideRequestRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class FindRideRequestServiceImplementation implements FindRideRequestService {
    @Autowired
    FindRideRequestRepository repository;

    @Override
    public FindRideRequest findById(int id) {
        return null;
    }

    @Override
    public FindRideRequest findLastUserRequest(long chatId) {
        return repository.findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(chatId).orElseThrow();
    }

    @Override
    public Optional<FindRideRequest> findLastUserRequestOptional(long chatId) {
        return repository.findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(chatId);
    }

    @Override
    public FindRideRequest addNewRequest(FindRideRequestDTO dto) {
        return null;
    }

    @Override
    public FindRideRequest updateRequest(FindRideRequest request) {
        return null;
    }

    @Override
    public List<FindRideRequest> usersRequestList(long chatId) {
        return null;
    }

    @Override
    public List<FindRideRequest> usersActiveRequestList(long chatId) {
        log.info("method usersActiveRequestList");

        return repository.findByUser_ChatIdAndIsActiveTrueOrderByCreatedAtAsc(chatId);
    }

    @Override
    public void cancelRequestById(int id) {

    }

    @Override
    public void cancelAllUsersActiveRequests(List<Integer> requestsIdList) {

    }
}
