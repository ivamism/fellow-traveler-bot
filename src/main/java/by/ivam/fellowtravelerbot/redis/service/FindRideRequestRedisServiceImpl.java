package by.ivam.fellowtravelerbot.redis.service;


import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.repository.FindRideRequestRedisRepository;
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
public class FindRideRequestRedisServiceImpl implements FindRideRequestRedisService {
    @Autowired
    FindRideRequestRedisRepository repository;


    @Override
    public void saveRequest(FindRideRequestRedis request) {
        log.info("method saveRedisRequest");
        repository.save(request);
    }


    @Override
    public FindRideRequestRedis findById(String id) {
        return repository.findById(id).orElseThrow();
    }


    @Override
    public Iterable<FindRideRequestRedis> findAll() {
        Iterable<FindRideRequestRedis> rides = repository.findAll();
        return rides;
    }

    @Override
    public List<FindRideRequestRedis> findAllByDirection(String direction) {
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


    public List<Integer> findMatches (FindPassRequestRedis recentRequest) {
//        FindPassRequestRedis receivedRequest = findPassRequestRedisService.findById(requestId);
        List<Integer> suitableRequestIdList = findAllByDirection(recentRequest.getDirection())
                .stream()
                .filter(request -> request.getDepartureBefore().isAfter(recentRequest.getDepartureAt()))
                .filter(request -> request.getDepartureBefore().toLocalDate().isEqual(recentRequest.getDepartureAt().toLocalDate()))
                .filter(request -> request.getPassengersQuantity() <= recentRequest.getSeatsQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
//        matchingHandler.sendListOfSuitableFindRideRequestMessage(suitableRequestIdList, receivedRequest);
        return suitableRequestIdList;
    }

    @Override
    public void removeExpired() {
        List<FindRideRequestRedis> expiredKeys = repository.findByExpireDuration(-1);
        if (expiredKeys.size()!=0){
            log.info("remove expired FindRideRequestRedis - " + expiredKeys.size());
            expiredKeys.forEach(request -> repository.deleteById(request.getRequestId()));
        }
    }

}
