package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.service.BookingService;
import by.ivam.fellowtravelerbot.servise.handler.MatchingHandler;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j
public class ScheduledJobs {
    @Autowired
    BookingService bookingService;

    @Autowired
    private MatchingHandler matchingHandler;

    @Scheduled(cron = "0 */2 * * * *")
    @Async
    public void checkBooking() {
        log.info("method checkBooking");
        List<Booking> bookingList = bookingService.findAll()
                .stream()
                .filter(booking -> booking.getRemindAt().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (!bookingList.isEmpty()) onExpireTimeToConfirm(bookingList);
        else log.debug("No bookings");
    }

    private void onExpireTimeToConfirm(List<Booking> bookingList) {
        log.info("method onExpireTimeToConfirm. bookings quantity" + bookingList.size());
        for (Booking booking: bookingList) {
            if (booking.getRemindersQuantity() < 2) {
                bookingService.incrementRemindsQuantityAndRemindTime(booking);
                matchingHandler.sendBookingAnnouncementMessage(booking);
            } else {
                log.debug("send announcement two times");
                bookingService.deleteBooking(booking);
                //TODO выслать инициатору новый список для резервирования
            }
        }
    }

}
