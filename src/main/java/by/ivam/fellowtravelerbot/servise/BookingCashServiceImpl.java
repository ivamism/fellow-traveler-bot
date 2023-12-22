package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.BookingCash;
import by.ivam.fellowtravelerbot.repository.BookingCashRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

public class BookingCashServiceImpl implements BookingCashService {

    @Autowired
    BookingCashRepository repository;

    @Override
    public void saveBookingState(BookingCash bookingCash) {
        repository.save(bookingCash);
    }

    @Override
    public Optional<BookingCash> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public void flushExpired() {
        repository.findByExpireAtBefore(LocalDateTime.now())
                .forEach(bookingCash -> repository.delete(bookingCash));
    }
}
