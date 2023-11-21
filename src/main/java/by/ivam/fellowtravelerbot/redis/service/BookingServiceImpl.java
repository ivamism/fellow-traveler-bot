package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.repository.BookingRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j
public class BookingServiceImpl implements BookingService{

    @Autowired
    private BookingRepository repository;


    @Override
    public Booking save(Booking booking) {
        repository.save(booking);
        return booking;
    }

    @Override
    public void removeExpired() {
        List<Booking> expiredKeys = repository.findByExpireDuration(-1);
        if (expiredKeys.size()!=0){
            log.info("remove expired FindRideRequestRedis - " + expiredKeys.size());
            expiredKeys.forEach(booking -> repository.deleteById(booking.getId()));
        }
    }
}
