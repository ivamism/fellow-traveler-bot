package by.ivam.fellowtravelerbot.redis.service;


import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.repository.FindPassRequestRedisRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j
public class FindPassRequestRedisServiceImpl implements FindPassRequestRedisService {
    private final FindPassRequestRedis EMPTY_RIDE = new FindPassRequestRedis();
    @Autowired
    FindPassRequestRedisRepository repository;

//    @Autowired
//    FindPassengerHandler findPassengerHandler;


    @Override
    public void saveRequest(FindPassRequestRedis request) {
        log.info("method saveRedisRequest");
        repository.save(request);
    }

    @Override
    public FindPassRequestRedis findById(String id) {
        return repository.findById(id).orElse(EMPTY_RIDE);
    }


    @Override
    public Iterable<FindPassRequestRedis> findAll() {
        Iterable<FindPassRequestRedis> rides = repository.findAll();
        return rides;
    }

    @Override
    public List<FindPassRequestRedis> findAllByDirection(String direction) {
        return repository.findAllByDirection(direction);
    }


    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public void getExpire(int requestId) {
//        findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
    }

    public List<Integer> findMatches (FindRideRequestRedis recentRequest) {
//        FindPassRequestRedis receivedRequest = findPassRequestRedisService.findById(requestId);
        List<Integer> suitableRequestIdList = findAllByDirection(recentRequest.getDirection())
                .stream()
                .filter(request -> request.getDepartureAt().toLocalDate().isEqual(recentRequest.getDepartureBefore().toLocalDate()))
                .filter(request -> request.getDepartureAt().isBefore(recentRequest.getDepartureBefore()))
                .filter(request -> request.getSeatsQuantity() >= recentRequest.getPassengersQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
//        matchingHandler.sendListOfSuitableFindRideRequestMessage(suitableRequestIdList, receivedRequest);
        return suitableRequestIdList;
    }

}
