package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    void addBooking(Pair<FindPassRequestRedis, FindRideRequestRedis> pairOfRequests, String initiator);

    Booking save(Booking booking);

    List<Booking> findAll();

    Booking findById(String bookingId);

    Optional<Booking> getBookingOptional(String bookingId);

    void incrementRemindsQuantityAndRemindTime(Booking booking);

    void deleteBooking(Booking booking);

    void deleteBookingDueToExpiredRequest(String  requestId, RequestsType requestsType);

    void deleteBooking(String bookingId);

    void deleteBookings(List<Booking> bookingsToDelete);

    boolean isNewBooking(Booking booking);

    void removeExpired();

    void removeBookingByCancelingRequest(RequestsType initiator, int requestId);
    void removeBookingByCancelingRequest(RequestsType initiator, String requestId);

    boolean hasBooking(RequestsType initiator, String requestId);
    List<Booking> getBookingsToDeleteOnCancelingRequest(RequestsType cancelInitiator, String stringRequestId);

}
