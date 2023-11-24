package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.repository.BookingRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Log4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository repository;

    @Override
    public Booking save(Booking booking) {
        repository.save(booking);
        return booking;
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> bookingList = StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .filter(booking -> Optional.ofNullable(booking).isPresent())
                .collect(Collectors.toList());
        return bookingList;
    }

    @Override
    public Booking findById(String bookingId) {
        return repository.findById(bookingId).orElseThrow();
    }

    @Override
    public void incrementRemindsQuantityAndRemindTime(Booking booking) {
        findById(booking.getId());
        booking.setRemindersQuantity(booking.getRemindersQuantity()+1);
        booking.setRemindAt(LocalDateTime.now().plusMinutes(1));
        repository.save(booking);
    }

    @Override
    public void deleteBooking(Booking booking) {
        log.debug("delete booking: " + booking);
        repository.delete(booking);
    }

    @Override
    public boolean isNewRequest(Booking booking) {
        return booking.getRemindersQuantity()==0;
    }

@Override
    public void removeExpired() {
        List<Booking> expiredKeys = repository.findByExpireDuration(-1);
        if (expiredKeys.size() != 0) {
            log.info("remove expired FindRideRequestRedis - " + expiredKeys.size());
            expiredKeys.forEach(booking -> deleteBooking(booking));
        }
    }


}
