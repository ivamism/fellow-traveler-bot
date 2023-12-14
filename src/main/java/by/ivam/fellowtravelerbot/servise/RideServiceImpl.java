package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.model.Ride;
import by.ivam.fellowtravelerbot.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

public class RideServiceImpl implements RideService {
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private FindRideRequestService findRideRequestService;
    @Autowired
    private FindPassengerRequestService findPassengerRequestService;

    @Override
    public Ride findById(int id) {
        return rideRepository.findById(id).orElseThrow();
    }


    @Override
    public Ride createNewRide(int findPassengerRequestId, Set<Integer> findRideRequestIdSet) {
        Ride ride = new Ride();
        ride.setFindPassengerRequest(findPassengerRequestService.findById(findPassengerRequestId));
        Set<FindRideRequest> findRideRequestSet = findRideRequestIdSet
                .stream()
                .map(id -> findRideRequestService.findById(id))
                .collect(Collectors.toSet());
        return rideRepository.save(ride);
    }

    @Override
    public Ride updateRide(int RideId) {
        return null;
    }

}
