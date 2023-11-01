package by.ivam.fellowtravelerbot.servise;

public interface MatchService {
    void cancelRequestById(int id);

    void getNewFindPassengerRequest(int requestId);

    void getNewFindRideRequest(int requestId);
}
