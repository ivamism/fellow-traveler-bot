package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.BookingCash;
import by.ivam.fellowtravelerbot.repository.BookingCashRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
@Service
@Log4j
public class BookingCashServiceImpl implements BookingCashService {

    @Autowired
    BookingCashRepository repository;

    @Override
    public void saveBookingState(BookingCash bookingCash) {
        repository.save(bookingCash);
        log.debug("Save bookingCash to DB: " + bookingCash);
    }

    @Override
    public Optional<BookingCash> findById(String id) {
        log.debug("method findById()");
        return repository.findById(id);
    }

    @Override
    public void flushExpired() {
        repository.findByExpireAtBefore(LocalDateTime.now())
                .forEach(bookingCash -> repository.delete(bookingCash));
        log.debug("method flushExpired()");
    }
}
