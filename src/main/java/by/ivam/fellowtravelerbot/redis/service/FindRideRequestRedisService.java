package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;

import java.util.List;
import java.util.Optional;

public interface FindRideRequestRedisService {
    void saveRequest(FindRideRequestRedis request);

    FindRideRequestRedis findById(String id);

    Optional<FindRideRequestRedis> getOptionalById(String id);

    Iterable<FindRideRequestRedis> findAll();

    List<FindRideRequestRedis> findAllNotExpired();

    List<FindRideRequestRedis> findAllByDirection(String direction);

    void delete(String id);

    void getExpire(int requestId);

    List<Integer> findMatches(FindPassRequestRedis receivedRequest);

    void removeExpired();

    Optional<FindRideRequestRedis> findOptionalById(String requestId);

}
