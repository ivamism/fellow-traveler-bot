package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.bot.enums.BookingInitiator;
import by.ivam.fellowtravelerbot.model.Ride;
import by.ivam.fellowtravelerbot.repository.RideRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static by.ivam.fellowtravelerbot.bot.enums.BookingInitiator.FIND_PASSENGER_REQUEST;
import static by.ivam.fellowtravelerbot.bot.enums.BookingInitiator.FIND_RIDE_REQUEST;

@Service
@Log4j
public class RideServiceImpl implements RideService {
    @Autowired
    private RideRepository repository;
    @Autowired
    private FindRideRequestService findRideRequestService;
    @Autowired
    private FindPassengerRequestService findPassengerRequestService;

    @Override
    public Ride findById(int id) {
        Ride ride = repository.findById(id).orElseThrow();
        log.debug("method findById " + ride);
        return ride;
    }

    @Override
    public Optional<Ride> getRideByFindPassengerRequestId(int id) {
        log.debug("method getRideByFindPassengerRequestId");
        return repository.findByFindPassengerRequest_Id(id);
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
        return repository.save(ride);
    }

    @Override
    public boolean hasRide(BookingInitiator initiator, int requestId) {
        if (initiator == FIND_PASSENGER_REQUEST) {
            return repository.existsByFindPassengerRequest_Id(requestId);
        } else if (initiator == FIND_RIDE_REQUEST) {
            return repository.existsByFindRideRequests_Id(requestId);
        } else {
            throw new IllegalArgumentException("Invalid BookingInitiator value: " + initiator);
        }
    }

}
