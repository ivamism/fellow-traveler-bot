package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.BookingCash;

import java.util.Optional;

public interface BookingCashService {
    void saveBookingState (BookingCash bookingCash);
    Optional<BookingCash> findById (String id);
    void flushExpired ();
}
