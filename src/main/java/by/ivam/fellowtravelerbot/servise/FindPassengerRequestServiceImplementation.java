package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.BookingService;
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
    private FindPassengerRequestRepository repository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private FindPassRequestRedisService redisService;

    @Autowired
    BookingTempService bookingTempService;


    @Override
    public FindPassengerRequest findById(int id) {
        return repository.findById(id).orElseThrow();
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
//        bookingTempService.setCanceledBy(RequestsType.FIND_PASSENGER_REQUEST, requestId);
        log.info("method cancelRequest. Request id: "+requestId);
        return repository.save(request);
    }

    @Override
    public FindPassengerRequest disActivateExpiredRequestById(int requestId) {
        log.info("method disActivateExpiredRequestById");
        FindPassengerRequest request = disActivateRequest(findById(requestId));
//        request.setActive(false);
//        repository.save(request);
//        String redisId = String.valueOf(requestId);
//        redisService.getOptionalById(redisId).ifPresent(requestRedis -> redisService.delete(redisId));
////        redisService.delete(redisId);
        return request;
    }

    private FindPassengerRequest disActivateRequest(FindPassengerRequest request) {
        log.info("method disActivateRequest");
        request.setActive(false);
        repository.save(request);
        String redisId = String.valueOf(request.getId());
        redisService.getOptionalById(redisId).ifPresent(requestRedis -> redisService.delete(redisId));
//        redisService.delete(redisId);
        return request;
    }

    @Override
    public void disActivateExpiredRequestsOnStart(LocalDateTime presentTime) {
        if (repository.count() != 0) {
            List<FindPassengerRequest> expiredRequestsList = repository.findByIsActiveTrueAndDepartureAtBefore(presentTime);
            if (expiredRequestsList.size() != 0) {
                log.info("dis-activate " + expiredRequestsList.size() + " FindPassengerRequests");
                expiredRequestsList.forEach(request -> disActivateRequest(request));
            }
        }
    }

    private void onSaveNewRequest(FindPassengerRequest request) {
        placeInRedis(request);
    }

    @Async
    public void placeInRedis(FindPassengerRequest request) {
        FindPassRequestRedis passRequestRedis = new FindPassRequestRedis();
        passRequestRedis.setRequestId(String.valueOf(request.getId()))
                .setChatId(request.getUser().getChatId())
                .setDirection(request.getDirection())
                .setDepartureAt(request.getDepartureAt())
                .setSeatsQuantity(request.getSeatsQuantity())
                .setExpireDuration(LocalDateTime.now().until(request.getDepartureAt(), ChronoUnit.SECONDS));
        log.debug("method placeInRedis");
        redisService.saveRequest(passRequestRedis);
    }

    public void removeFromRedis(int requestId) {
        log.debug("method removeFromRedis");
        String id = String.valueOf(requestId);
        redisService.getOptionalById(id).ifPresent(request -> redisService.delete(id));
        log.debug("delete request with Id :" + id + " from redis");
    }

}
