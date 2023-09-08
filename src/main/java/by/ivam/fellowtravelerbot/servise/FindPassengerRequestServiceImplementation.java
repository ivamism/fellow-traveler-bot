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
        return null;
    }

    @Override
    public Optional <FindPassengerRequest> findLastUserRequest(long chatId) {
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
                .setDepartureAt(LocalDateTime.of(dto.getDepartureDate(),dto.getDepartureTime()))
                .setCar(dto.getCar())
                .setSeatsQuantity(dto.getSeatsQuantity())
                .setCommentary(dto.getCommentary())
                .setActive(true)
                .setCreatedAt(LocalDateTime.now())
                .setCanceled(false);
//                .setCanceledAt(LocalDateTime.of(0,0,0,0,0));

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
        return repository.findByUser_ChatIdAndIsActiveTrueOrderByDepartureAtAsc(chatId);
    }

    @Override
    public void canselRequestById(int id) {

    }

    @Override
    public void cancelAllUsersActiveRequests(List<Integer> requestsIdList) {

    }
}
