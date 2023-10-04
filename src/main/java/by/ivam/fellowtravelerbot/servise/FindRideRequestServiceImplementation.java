package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.repository.FindRideRequestRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class FindRideRequestServiceImplementation implements FindRideRequestService {
    @Autowired
    private FindRideRequestRepository repository;

    @Override
    public FindRideRequest findById(int id) {
        log.info("method findById");
        return repository.findById(id).orElseThrow();
    }

    @Override
    public FindRideRequest findLastUserRequest(long chatId) {
        log.info("method findLastUserRequest");
        return repository.findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(chatId).orElseThrow();
    }

    @Override
    public Optional<FindRideRequest> findLastUserRequestOptional(long chatId) {
        log.info("method findLastUserRequestOptional");
        return repository.findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(chatId);
    }

    @Override
    public FindRideRequest addNewRequest(FindRideRequestDTO dto) {
        FindRideRequest request = new FindRideRequest();
        request.setUser(dto.getUser())
                .setDirection(dto.getDirection())
                .setDepartureSettlement(dto.getDepartureSettlement())
                .setDestinationSettlement(dto.getDestinationSettlement())
                .setDepartureBefore(dto.getDepartureBefore())
                .setPassengersQuantity(dto.getPassengersQuantity())
                .setCommentary(dto.getCommentary())
                .setActive(true)
                .setCreatedAt(LocalDateTime.now());
        log.info("method addNewRequest. Saved new request: " + request);
        return repository.save(request);
    }

    @Override
    public FindRideRequest updateRequest(FindRideRequest request) {
        log.info("method updateRequest");
        return repository.save(request);
    }

    @Override
    public List<FindRideRequest> usersRequestList(long chatId) {
        log.info("method usersRequestList");
        return repository.findByUser_ChatIdAndIsActiveTrueOrderByCreatedAtAsc(chatId);
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
