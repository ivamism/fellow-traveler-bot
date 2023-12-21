package by.ivam.fellowtravelerbot.redis.repository;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends CrudRepository<Booking, String>  {
    List<Booking> findByExpireDuration(long expireDuration);

    @Override
    Optional<Booking> findById(String handlerPrefix);

    boolean existsByFindPassRequestRedis_RequestId(String requestId);

    boolean existsByFindRideRequestRedis_RequestId(String requestId);


}
