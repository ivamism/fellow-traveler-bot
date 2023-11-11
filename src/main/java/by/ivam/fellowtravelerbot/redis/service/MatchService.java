package by.ivam.fellowtravelerbot.redis.service;

public interface MatchService {
    void getNewFindPassengerRequest(String requestId);

    void getNewFindRideRequest(String requestId);
}
