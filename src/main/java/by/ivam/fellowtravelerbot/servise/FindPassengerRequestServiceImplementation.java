package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.FindPassRequestRedisService;
import by.ivam.fellowtravelerbot.repository.FindPassengerRequestRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j
public class FindPassengerRequestServiceImplementation implements FindPassengerRequestService {
    @Autowired
    FindPassengerRequestRepository repository;
    @Autowired
    FindPassRequestRedisService redisService;


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
                .setCanceled(false);
//                .setCanceledAt(LocalDateTime.of(1, 1, 1, 1, 1));

        log.info("method addNewRequest. Saved new request: " + request);
        repository.save(request);
        onSaveNewRequest(request);
        return request;
    }

    @Override
    public FindPassengerRequest updateRequest(FindPassengerRequest request) {
        return repository.save(request);
    }

    @Override
    public List<FindPassengerRequest> usersRequestList(long chatId) {
        return null;
    }

    @Override
    public List<FindPassengerRequest> requestListByIdList(List<Integer> requestsIdList) {
        List<FindPassengerRequest> requestList = requestsIdList
                .stream()
                .map(id -> findById(id))
                .collect(Collectors.toList());
        return requestList;
    }

    @Override
    public List<FindPassengerRequest> usersActiveRequestList(long chatId) {
        log.info("method usersActiveRequestList");
        return repository.findByUser_ChatIdAndIsActiveTrueOrderByDepartureAtAsc(chatId);
    }

    @Override
    public FindPassengerRequest cancelRequestById(int requestId) {
        FindPassengerRequest request = findById(requestId);
        request.setActive(false)
                .setCanceled(true)
                .setCanceledAt(LocalDateTime.now());
        log.info("method cancelRequest");
        return repository.save(request);
    }

    @Override
    public FindPassengerRequest disActivateRequestById(int requestId) {
        log.info("method disActivateRequestById");
        FindPassengerRequest request = findById(requestId);
        request.setActive(false);
        repository.save(request);
        return request;
    }

    @Override
    public void disActivateExpiredRequests(LocalDateTime presentTime) {
        if (repository.count() != 0) {
            List<FindPassengerRequest> expiredRequestsList = repository.findByIsActiveTrueAndDepartureAtBefore(presentTime);
            if (expiredRequestsList.size() != 0) {
                log.info("dis-activate " + expiredRequestsList.size() + " FindPassengerRequests");
                expiredRequestsList.forEach(request -> repository.save(request.setActive(false)));
            }
        }
    }

    @Override
    public FindPassengerRequest disActivateRequestById(int requestId) {
        log.info("method disActivateRequestById");
        FindPassengerRequest request = findById(requestId);
        request.setActive(false);
        repository.save(request);
        return request;
    }

    @Override
    public void cancelAllUsersActiveRequests(List<Integer> requestsIdList) {
    }

    private void onSaveNewRequest(FindPassengerRequest request) {
        placeInRedis(request);
    }

    @Async
    public void placeInRedis(FindPassengerRequest request) {
        FindPassRequestRedis passRequestRedis = new FindPassRequestRedis();
        passRequestRedis.setRequestId(Integer.toString(request.getId()))
                .setChatId(request.getUser().getChatId())
                .setDirection(request.getDirection())
                .setDepartureAt(request.getDepartureAt())
                .setSeatsQuantity(request.getSeatsQuantity())
                .setExpireDuration(LocalDateTime.now().until(request.getDepartureAt(), ChronoUnit.SECONDS));
        log.info("method placeInRedis");
        redisService.saveRequest(passRequestRedis);
    }

}
