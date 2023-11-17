package by.ivam.fellowtravelerbot.redis.repository;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import org.springframework.data.repository.CrudRepository;

public interface BookingRepository extends CrudRepository<Booking, String>  {

}
