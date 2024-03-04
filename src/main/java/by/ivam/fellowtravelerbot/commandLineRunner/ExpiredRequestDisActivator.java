package by.ivam.fellowtravelerbot.commandLineRunner;

import by.ivam.fellowtravelerbot.servise.FindPassengerRequestService;
import by.ivam.fellowtravelerbot.servise.FindRideRequestService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(2)
@Async
@Log4j
public class ExpiredRequestDisActivator implements CommandLineRunner {
    @Autowired
    FindPassengerRequestService findPassengerRequestService;
    @Autowired
    FindRideRequestService findRideRequestService;

    @Override
    public void run(String... args) {
        try {
            log.debug("Dis-activation of expired requests");
            findRideRequestService.disActivateExpiredRequests(LocalDateTime.now());
            findPassengerRequestService.disActivateExpiredRequestsOnStart(LocalDateTime.now());
        } catch (Exception e) {
            log.error("An error occurred while dis-activating expired requests", e);
        }
    }
}
