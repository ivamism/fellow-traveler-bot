package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Ride;

import java.util.Set;

public interface RideService {
    Ride findById(int id);

    Ride createNewRide (int findPassengerRequestId, Set<Integer> findRideRequestIdSet) ;
    Ride updateRide(int RideId);

}
