package by.ivam.fellowtravelerbot.redis.repository;

import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FindPassRequestRedisRepository extends CrudRepository<FindPassRequestRedis, String> {
    List<FindPassRequestRedis> findAllByDirection(String direction);

    List<FindPassRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt);

    List<FindPassRequestRedis> findByDirectionAndDepartureAtBeforeOrderByDepartureAtAsc(String direction, LocalDateTime DepartureAt);

    List<FindPassRequestRedis> findByDirectionAndDepartureAtBeforeAndSeatsQuantityGreaterThanEqual(String direction, LocalDateTime DepartureAt, int seatsQuantity);

    List<FindPassRequestRedis> findByDirectionAndSeatsQuantityGreaterThanEqual(String direction, int seatsQuantity);

    List<FindPassRequestRedis> findByDirectionAndSeatsQuantity(String direction, int seatsQuantity);

    List<FindPassRequestRedis> findByExpireDuration(long expireDuration);



}
