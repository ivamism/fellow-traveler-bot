package by.ivam.fellowtravelerbot.servise;

public interface MatchService {

    void getNewFindPassengerRequest(String requestId);

    void getNewFindRideRequest(String requestId);

    //    void addBooking(Booking booking);
    void addBooking(String findPassRequestId, String findRideRequestId, String initiator);
}