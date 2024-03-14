package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;

import java.util.List;
import java.util.Optional;

public interface FindPassRequestRedisService {
    void saveRequest(FindPassRequestRedis request);

    FindPassRequestRedis findById(String id);

    Optional<FindPassRequestRedis> getOptionalById(String id);

    Iterable<FindPassRequestRedis> findAll();

    List<FindPassRequestRedis> findAllNotExpired();

    List<FindPassRequestRedis> findAllByDirection(String direction);

    void delete(String id);

    List<Integer> findMatches(FindRideRequestRedis recentRequest);

    void removeExpired();

    void updateSeatsQuantity(FindPassRequestRedis request, int passengersQuantity);

}
