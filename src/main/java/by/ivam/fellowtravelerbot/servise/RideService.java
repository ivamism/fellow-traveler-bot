package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.Ride;

import java.util.Optional;
import java.util.Set;

public interface RideService {
    Ride findById(int id);

    Optional<Ride> getRideByFindPassengerRequestId(int id);

    Ride createNewRide(int findPassengerRequestId, Set<Integer> findRideRequestIdSet);

    Ride createNewRide(int findPassengerRequestId, int findRideRequestId);

    Ride saveRide(Ride ride);
    boolean hasRide(RequestsType cancellationInitiator, int requestId);


}
