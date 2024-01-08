package by.ivam.fellowtravelerbot.redis.repository;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BookingRepository extends CrudRepository<Booking, String>  {
    List<Booking> findByExpireDuration(long expireDuration);

    @Override
    Optional<Booking> findById(String handlerPrefix);

    List<Booking> findByFindPassRequestRedis_RequestId(String requestId);

    List<Booking> findByFindRideRequestRedis_RequestId(String requestId);
    boolean existsByFindPassRequestRedis_RequestId(String requestId);

    boolean existsByFindRideRequestRedis_RequestId(String requestId);
}
