package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.repository.BookingTempRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class BookingTempServiceImpl implements BookingTempService {

    @Autowired
    BookingTempRepository repository;

    @Override
    public void saveBookingTemp(Booking booking) {
        BookingTemp bookingTemp = new BookingTemp();
        LocalDateTime bookedAt = booking.getBookedAt();
        bookingTemp.setId(booking.getId())
                .setFindPassengerRequestId(Integer.valueOf(booking.getFindPassRequestRedis().getRequestId()))
                .setFindRideRequestId(Integer.valueOf(booking.getFindRideRequestRedis().getRequestId()))
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

    @Override
    public void setCanceledBy(RequestsType type, int requestId) {
        log.debug("method setCanceledBy");
        if (type == RequestsType.FIND_PASSENGER_REQUEST) {
            repository.findByFindPassengerRequestId(requestId)
                    .stream()
                    .peek(bookingTemp -> bookingTemp.setCanceledBy(type))
                    .forEach(bookingTemp -> repository.save(bookingTemp));
        } else if (type == RequestsType.FIND_RIDE_REQUEST) {
            repository.findByFindRideRequestId(requestId).ifPresent(bookingTemp -> {
                bookingTemp.setCanceledBy(type);
                repository.save(bookingTemp);
            });
        }
    }


    @Scheduled(cron = "0 0 3 * * *")
    @Async
    @Override
    public void flushExpired() {
        long deleted = repository.deleteByExpireAtBefore(LocalDateTime.now());
        log.debug("method flushExpired(), deleted entities: " + deleted);
    }
}
