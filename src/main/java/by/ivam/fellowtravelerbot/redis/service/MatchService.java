package by.ivam.fellowtravelerbot.redis.service;

public interface MatchService {
    void cancelRequestById(int id);

    void getNewFindPassengerRequest(int requestId);

    void getNewFindRideRequest(int requestId);
}
