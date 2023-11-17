package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.repository.BookingRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
