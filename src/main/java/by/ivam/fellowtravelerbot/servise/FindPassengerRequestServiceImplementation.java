package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.repository.FindPassengerRequestRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class FindPassengerRequestServiceImplementation implements FindPassengerRequestService {
    @Autowired
    FindPassengerRequestRepository repository;

    @Override
    public FindPassengerRequest findById(int id) {
        return repository.findById(id).get();
    }

    @Override
    public FindPassengerRequest findLastUserRequest(long chatId) {
        log.info("method findLastUserRequest");
        FindPassengerRequest request = repository.findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(chatId).orElseThrow();
        return request;
    }

    @Override
    public Optional<FindPassengerRequest> findLastUserRequestOptional(long chatId) {
        log.info("method findLastUserRequestOptional");
        return repository.findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(chatId);
    }


    @Override
    public FindPassengerRequest addNewRequest(FindPassengerRequestDTO dto) {
        FindPassengerRequest request = new FindPassengerRequest();
        request.setUser(dto.getUser())
                .setDepartureSettlement(dto.getDepartureSettlement())
                .setDirection(dto.getDirection())
                .setDepartureLocation(dto.getDepartureLocation())
                .setDestinationSettlement(dto.getDestinationSettlement())
                .setDestinationLocation(dto.getDestinationLocation())
                .setDepartureAt(LocalDateTime.of(dto.getDepartureDate(), dto.getDepartureTime()))
                .setCar(dto.getCar())
                .setSeatsQuantity(dto.getSeatsQuantity())
                .setCommentary(dto.getCommentary())
                .setActive(true)
                .setCreatedAt(LocalDateTime.now())
                .setCanceled(false)
                .setCanceledAt(LocalDateTime.of(1, 1, 1, 1, 1));

        log.info("method addNewRequest. Saved new request: " + request);
        return repository.save(request);
    }

    @Override
    public FindPassengerRequest updateRequest(FindPassengerRequest request) {
        return null;
    }

    @Override
    public List<FindPassengerRequest> usersRequestList(long chatId) {
        return null;
    }

    @Override
    public List<FindPassengerRequest> usersActiveRequestList(long chatId) {
        log.info("method usersActiveRequestList");
        return repository.findByUser_ChatIdAndIsActiveTrueOrderByDepartureAtAsc(chatId);
    }

    @Override
    public void cancelRequestById(int id) {

    }

    @Override
    public void cancelAllUsersActiveRequests(List<Integer> requestsIdList) {

    }
}