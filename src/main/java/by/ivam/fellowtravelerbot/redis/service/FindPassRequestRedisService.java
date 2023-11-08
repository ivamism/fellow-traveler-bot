package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;

import java.time.LocalDateTime;
import java.util.List;

public interface FindPassRequestRedisService {
    void saveRequest(FindPassRequestRedis request);

    FindPassRequestRedis findById(String id);

    Iterable<FindPassRequestRedis> findAll();

    List<FindPassRequestRedis> findAllByDirection(String direction);

    List<FindPassRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt);

    void delete(String id);

    void getExpire(int requestId);

    List<FindPassRequestRedis> findMatches(int id);
}
