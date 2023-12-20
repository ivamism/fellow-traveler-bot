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
import java.util.Optional;
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
    public Optional<FindPassRequestRedis> findOptionalById(String id) {
        return repository.findById(id);
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

    public List<Integer> findMatches(FindRideRequestRedis recentRequest) {
        List<Integer> suitableRequestIdList = findAllByDirection(recentRequest.getDirection())
                .stream()
                .filter(request -> request.getDepartureAt().toLocalDate().isEqual(recentRequest.getDepartureBefore().toLocalDate()))
                .filter(request -> request.getDepartureAt().isBefore(recentRequest.getDepartureBefore()))
                .filter(request -> request.getSeatsQuantity() >= recentRequest.getPassengersQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
        return suitableRequestIdList;
    }

    @Override
    public void removeExpired() {
        List<FindPassRequestRedis> expiredKeys = repository.findByExpireDuration(-1);
        if (expiredKeys.size() != 0) {
            log.info("remove expired FindPassRequestRedis - " + expiredKeys.size());
            expiredKeys.forEach(request -> repository.delete(request));
        }
    }

    @Override
    public void updateSeatsQuantity(FindPassRequestRedis request, int passengersQuantity) {
        request.setSeatsQuantity(request.getSeatsQuantity() - passengersQuantity);
        repository.save(request);
    }

}
