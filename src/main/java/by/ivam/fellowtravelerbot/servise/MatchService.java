package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.FindRideRequest;

public interface MatchService {
    void getNewFindPassengerRequest(String requestId);

    void getNewFindPassengerRequest(FindPassengerRequest receivedRequest);

    void getNewFindRideRequest(String requestId);

    void getNewFindRideRequest(FindRideRequest receivedRequest);
}