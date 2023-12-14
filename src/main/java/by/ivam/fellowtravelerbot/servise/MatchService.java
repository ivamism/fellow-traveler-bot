package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Ride;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;

import java.util.List;

public interface MatchService {
    void getNewFindPassengerRequest(String requestId);

    void getNewFindRideRequest(String requestId);

    void addBooking(String findPassRequestId, String findRideRequestId, String initiator);

    void deleteBooking(String bookingId);

    Booking getBooking(String bookingId);

    List<Integer> getFindPassRequestMatches(FindRideRequestRedis request);

    List<Integer> getFindRideRequestMatches(FindPassRequestRedis request);

    Ride createOrUpdateRide(String bookingId);
}