package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;

import java.util.Optional;

public interface BookingCashService {
    void saveBookingState (Booking booking);
    Optional<BookingTemp> findById (String id);
    void flushExpired ();
}
