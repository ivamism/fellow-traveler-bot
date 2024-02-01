package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.repository.BookingTempRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Log4j2
public class BookingTempServiceImpl implements BookingTempService {

    @Autowired
    BookingTempRepository repository;

    @Override
    public void saveBookingTemp(Booking booking) {
        BookingTemp bookingTemp = new BookingTemp();
        LocalDateTime bookedAt = booking.getBookedAt();
        bookingTemp.setId(booking.getId())
                .setFindPassengerRequestId(Integer.parseInt(booking.getFindPassRequestRedis().getRequestId()))
                .setFindRideRequestId(Integer.parseInt(booking.getFindRideRequestRedis().getRequestId()))
                .setBookedAt(bookedAt)
                .setExpireAt(bookedAt.plusSeconds(booking.getExpireDuration()));
        repository.save(bookingTemp);
        log.debug("Save bookingTemp to DB: " + bookingTemp);
    }


    @Override
    public Optional<BookingTemp> findById(String id) {
        log.debug("method findById()");
        return repository.findById(id);
    }
    @Scheduled(cron = "0 0 3 * * *")
    @Async
    @Override
    public void flushExpired() {

        long deleted = repository.deleteByExpireAtBefore(LocalDateTime.now());
        log.debug("method flushExpired(), deleted entities: " + deleted);
    }
}
