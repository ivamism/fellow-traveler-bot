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
public class FindPassRequestRedisService {
    private final FindPassRequestRedis EMPTY_RIDE = new FindPassRequestRedis();
    @Autowired
    FindPassRequestRedisRepository repository;

    public void saveRedisRequest(FindPassRequestRedis request) {
        log.info("method saveRedisRequest");
        repository.save(request);
    }

//    public void addRide(Dto dto){
//        saveRide(criateRide(dto));
//    }
//
//    Ride criateRide(Dto dto){
//        Ride ride = new Ride();
//        ride.setId(Integer.toString(dto.getId()))
//                .setDirection(dto.getDirection())
//                .setDepartureAt(dto.getDepartureAt())
//                .setSeatsQuantity(dto.getSeatsQuantity())
//                .setExpireDuration(LocalDateTime.now().until(ride.getDepartureAt(), ChronoUnit.SECONDS));
//        return ride;
//    }

    public FindPassRequestRedis findById(String id) {
        return repository.findById(id).orElse(EMPTY_RIDE);
    }


    public Iterable<FindPassRequestRedis> findAll() {
        Iterable<FindPassRequestRedis> rides = repository.findAll();
        return rides;
    }

    public List<FindPassRequestRedis> findAllByDirection(String direction) {
        return repository.findAllByDirection(direction);
    }

    public List<FindPassRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt) {
        return repository.findAllByDirectionAndDepartureAt(direction, departureAt);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}
