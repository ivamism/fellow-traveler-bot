package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.FindRideRequestRedisService;
import by.ivam.fellowtravelerbot.repository.FindRideRequestRepository;
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
public class FindRideRequestServiceImplementation implements FindRideRequestService {
    @Autowired
    private FindRideRequestRepository repository;
    @Autowired
    private FindRideRequestRedisService redisService;


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
        repository.save(request);
        onSaveNewRequest(request);
        return request;
    }

    @Override
    public FindRideRequest updateRequest(FindRideRequest request) {
        log.info("method updateRequest");
        return repository.save(request);
    }

    @Override
    public List<FindRideRequest> usersActiveRequestList(long chatId) {
        log.info("method usersActiveRequestList");
        return repository.findByUser_ChatIdAndIsActiveTrueOrderByCreatedAtAsc(chatId);
    }

    @Override
    public List<FindRideRequest> requestListByIdList(List<Integer> requestIdList) {
        List<FindRideRequest> requestList = requestIdList
                .stream()
                .map(id -> findById(id))
                .collect(Collectors.toList());
        return requestList;
    }

    @Override
    public FindRideRequest cancelRequestById(int requestId) {
        FindRideRequest request = findById(requestId);
        request.setActive(false)
                .setCanceled(true)
                .setCanceledAt(LocalDateTime.now());
        removeFromRedis(requestId);
        log.debug("method cancelRequestById");
        return repository.save(request);
    }

    @Async
    public void placeInRedis(FindRideRequest request) {
        FindRideRequestRedis rideRequestRedis = new FindRideRequestRedis();
        rideRequestRedis.setRequestId(String.valueOf(request.getId()))
                .setChatId(request.getUser().getChatId())
                .setDirection(request.getDirection())
                .setDepartureBefore(request.getDepartureBefore())
                .setPassengersQuantity(request.getPassengersQuantity())
                .setExpireDuration(LocalDateTime.now().until(request.getDepartureBefore(), ChronoUnit.SECONDS));
        log.info("method placeInRedis");
        redisService.saveRequest(rideRequestRedis);
    }

    private void removeFromRedis(int requestId) {
        String id = String.valueOf(requestId);
        redisService.findOptionalById(id).ifPresent(request -> redisService.delete(id));
        log.debug("delete request with Id :" + id + " from redis");
    }

    private void onSaveNewRequest(FindRideRequest request) {
        placeInRedis(request);
    }

    @Override
    public FindRideRequest disActivateRequestById(int requestId) {
        log.info("method disActivateRequestById");
        FindRideRequest request = findById(requestId);
        request.setActive(false);
        removeFromRedis(requestId);
        return repository.save(request);
    }

    @Override
    public void disActivateExpiredRequests(LocalDateTime presentTime) {
        if (repository.count() != 0) {
            List<FindRideRequest> expiredRequestsList = repository.findByIsActiveTrueAndDepartureBeforeBefore(presentTime);
            if (expiredRequestsList.size() != 0) {
                log.info("dis-activate " + expiredRequestsList.size() + " FindRideRequests");
                expiredRequestsList.forEach(request -> repository.save(request.setActive(false)));
            }
        }
    }
}

