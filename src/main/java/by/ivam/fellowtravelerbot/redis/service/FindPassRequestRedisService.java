package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;

import java.util.List;

public interface FindPassRequestRedisService {
    void saveRequest(FindPassRequestRedis request);

    FindPassRequestRedis findById(String id);

    Iterable<FindPassRequestRedis> findAll();

    List<FindPassRequestRedis> findAllByDirection(String direction);

    void delete(String id);

    void getExpire(int requestId);

    List<Integer> findMatches(FindRideRequestRedis recentRequest);

    void removeExpired();
}
