package by.ivam.fellowtravelerbot.redis.service;


import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.repository.FindRideRequestRedisRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

//    @Override
//    public List<FindRideRequestRedis> findAllByDirection(String direction) {
//        return repository.findAllByDirection(direction);
//    }
//
//    @Override
//    public List<FindRideRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt) {
//        return repository.findAllByDirectionAndDepartureAt(direction, departureAt);
//    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public void getExpire(int requestId) {
//        findPassengerHandler.sendExpireDepartureTimeMessage(requestId);
    }

    @Override
    public List<Long> findMatches(String direction, LocalDateTime time, int seatsQuantity) {
//        List<FindRideRequestRedis> matches = repository
//                .findByDirectionAndDepartureBeforeBeforeAndPassengersQuantityGreaterThanEqual(direction, time, seatsQuantity);
//        log.info("Method findMatches. Matches found: " + matches.toString());
//        return matches;
        LocalDate date = time.toLocalDate();
        List<Long> matchedChatIdList =
                repository.findAllByDirection(direction)
                        .stream()
                        .filter(request -> request.getDepartureBefore().isBefore(time))
                        .filter(request -> request.getDepartureBefore().toLocalDate().isEqual(date))
                        .filter(request -> request.getPassengersQuantity()<=seatsQuantity)
                        .map(request -> request.getChatId())
                        .collect(Collectors.toList());
        log.info("Method findMatches. Matches found: " + matchedChatIdList.toString());

        return matchedChatIdList;
    }
}
