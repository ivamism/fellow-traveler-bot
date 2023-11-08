package by.ivam.fellowtravelerbot.redis.service;


import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.repository.FindRideRequestRedisRepository;
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
public class FindRideRequestRedisServiceImpl implements FindRideRequestRedisService {
    private final FindRideRequestRedis EMPTY_RIDE = new FindRideRequestRedis();
    @Autowired
    FindRideRequestRedisRepository repository;

//    @Autowired
//    FindPassengerHandler findPassengerHandler;


    @Override
    public void saveRequest(FindRideRequestRedis request) {
        log.info("method saveRedisRequest");
        repository.save(request);
    }

//    public void addRide(FindPassRequestRedisDto dto){
//        saveRide(createRide(dto));
//    }
//
//    Ride createRide(FindPassRequestRedisDto dto){
//        Ride ride = new Ride();
//        ride.setId(Integer.toString(dto.getId()))
//                .setDirection(dto.getDirection())
//                .setDepartureAt(dto.getDepartureAt())
//                .setSeatsQuantity(dto.getSeatsQuantity())
//                .setExpireDuration(LocalDateTime.now().until(ride.getDepartureAt(), ChronoUnit.SECONDS));
//        return ride;
//    }

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
    public List<FindRideRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt) {
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
    public List<FindRideRequestRedis> findMatches(int id){
        FindRideRequestRedis passRequestRedis = findById(Integer.toString(id));
        String direction = passRequestRedis.getDirection();
        LocalDateTime departureAt = passRequestRedis.getDepartureBefore().plusHours(2);
        List<FindRideRequestRedis> matches = repository.findByDirectionAndDepartureAtBeforeOrderByDepartureAtAsc(direction, departureAt);
        log.info("Method findMatches. Matches found: " + matches.toString());

        return matches;
    }
}
