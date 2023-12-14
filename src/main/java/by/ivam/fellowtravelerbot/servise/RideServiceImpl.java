package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Ride;
import by.ivam.fellowtravelerbot.repository.RideRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j
public class RideServiceImpl implements RideService {
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private FindRideRequestService findRideRequestService;
    @Autowired
    private FindPassengerRequestService findPassengerRequestService;

    @Override
    public Ride findById(int id) {
        Ride ride = rideRepository.findById(id).orElseThrow();
        log.debug("method findById " + ride);
        return ride;
    }

    @Override
    public Optional<Ride> getRideByFindPassengerRequestId(int id) {
        log.debug("method getRideByFindPassengerRequestId");
        return rideRepository.findByFindPassengerRequest_Id(id);
    }


    @Override
    public Ride createNewRide(int findPassengerRequestId, Set<Integer> findRideRequestIdSet) {
        Ride ride = new Ride();
        ride.setFindPassengerRequest(findPassengerRequestService.findById(findPassengerRequestId));
        ride.setFindRideRequests(findRideRequestIdSet
                .stream()
                .map(id -> findRideRequestService.findById(id))
                .collect(Collectors.toSet()));
        log.debug("method createNewRide " + ride);
        return ride;
    }

    @Override
    public Ride createNewRide(int findPassengerRequestId, int findRideRequestId) {
        Ride ride = new Ride();
        ride.setFindPassengerRequest(findPassengerRequestService.findById(findPassengerRequestId));
        ride.setFindRideRequests(Set.of(findRideRequestService.findById(findRideRequestId)));
        log.debug("method createNewRide " + ride);

        return ride;
    }

    @Override
    public Ride saveRide(Ride ride) {
        log.debug("method saveRide " + ride);
        return rideRepository.save(ride);
    }

}
