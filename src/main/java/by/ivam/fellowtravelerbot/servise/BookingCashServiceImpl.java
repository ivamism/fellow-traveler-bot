package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.BookingCash;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.repository.BookingCashRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Log4j
public class BookingCashServiceImpl implements BookingCashService {

    @Autowired
    BookingCashRepository repository;

    @Override
    public void saveBookingState(Booking booking) {
        BookingCash bookingCash = new BookingCash();
        LocalDateTime bookedAt = booking.getBookedAt();
        bookingCash.setFindPassengerRequestId(Integer.parseInt(booking.getFindPassRequestRedis().getRequestId()))
                .setFindRideRequestId(Integer.parseInt(booking.getFindRideRequestRedis().getRequestId()))
                .setBookedAt(bookedAt)
                .setExpireAt(bookedAt.plusSeconds(booking.getExpireDuration()));
        repository.save(bookingCash);
        log.debug("Save bookingCash to DB: " + bookingCash);
    }

    @Override
    public Optional<BookingCash> findById(String id) {
        log.debug("method findById()");
        return repository.findById(id);
    }
    @Scheduled(cron = "0 0 3 * * *")
    @Async
    @Override
    public void flushExpired() {
        repository.findByExpireAtBefore(LocalDateTime.now())
                .forEach(bookingCash -> repository.delete(bookingCash));
        log.debug("method flushExpired()");
    }
}
