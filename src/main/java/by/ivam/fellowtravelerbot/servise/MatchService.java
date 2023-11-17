package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.redis.model.Booking;

public interface MatchService {
    void getNewFindPassengerRequest(String requestId);
    void getNewFindRideRequest(String requestId);
void addBooking(Booking booking);
}