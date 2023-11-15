package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;

import java.time.LocalDateTime;
import java.util.List;

public interface FindRideRequestRedisService {
    void saveRequest(FindRideRequestRedis request);

    FindRideRequestRedis findById(String id);

    Iterable<FindRideRequestRedis> findAll();

    List<FindRideRequestRedis> findAllByDirection(String direction);
//
//    List<FindRideRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt);

    void delete(String id);

    void getExpire(int requestId);

//    List<FindRideRequestRedis> findMatches(int id);

    List<Long> findMatches(String direction, LocalDateTime time, int seatsQuantity);
}
