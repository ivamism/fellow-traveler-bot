package by.ivam.fellowtravelerbot.redis.service;


import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.repository.FindPassRequestRedisRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    public void saveRedisRequest(FindPassRequestRedis request) {
        log.info("method saveRedisRequest");
        repository.save(request);
    }

//    public void addRide(Dto dto){
//        saveRide(createRide(dto));
//    }
//
//    Ride createRide(Dto dto){
//        Ride ride = new Ride();
//        ride.setId(Integer.toString(dto.getId()))
//                .setDirection(dto.getDirection())
//                .setDepartureAt(dto.getDepartureAt())
//                .setSeatsQuantity(dto.getSeatsQuantity())
//                .setExpireDuration(LocalDateTime.now().until(ride.getDepartureAt(), ChronoUnit.SECONDS));
//        return ride;
//    }

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
    public List<FindPassRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt) {
        return repository.findAllByDirectionAndDepartureAt(direction, departureAt);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public void getExpire(int requestId) {
//        findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
    }

    @Override
    public List<FindPassRequestRedis> findMatches(int id){
        FindPassRequestRedis passRequestRedis = findById(Integer.toString(id));
        String direction = passRequestRedis.getDirection();
        LocalDateTime departureAt = passRequestRedis.getDepartureAt().plusHours(2);
        List<FindPassRequestRedis> matches = repository.findByDirectionAndDepartureAtBeforeOrderByDepartureAtAsc(direction, departureAt);
        log.info("Method findMatches. Matches found: " + matches.toString());

        return matches;
    }
}
