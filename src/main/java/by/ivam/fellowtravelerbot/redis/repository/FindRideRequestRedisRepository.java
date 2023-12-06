package by.ivam.fellowtravelerbot.redis.repository;

import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FindRideRequestRedisRepository extends CrudRepository<FindRideRequestRedis, String> {
    List<FindRideRequestRedis> findAllByDirection(String direction);
//
//    List<FindRideRequestRedis> findAllByDirectionAndDepartureAt(String direction, LocalDateTime departureAt);
//
//    List<FindRideRequestRedis> findByDirectionAndDepartureAtBeforeOrderByDepartureAtAsc(String direction, LocalDateTime DepartureAt);

    List<FindRideRequestRedis> findByDirectionAndDepartureBeforeBeforeAndPassengersQuantityGreaterThanEqual(String direction, LocalDateTime DepartureBefore, int passengersQuantity);

    List<FindRideRequestRedis> findByDirectionAndPassengersQuantityLessThanEqual(String direction, int passengersQuantity);

    List<FindRideRequestRedis> findByExpireDuration(long expireDuration);

}
