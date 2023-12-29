package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.BookingCash;
import by.ivam.fellowtravelerbot.redis.model.Booking;

import java.util.Optional;

public interface BookingCashService {
    void saveBookingState (Booking booking);
    Optional<BookingCash> findById (String id);
    void flushExpired ();
}
