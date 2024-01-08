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
    private BookingService bookingService;
//    @Autowired
//    private BookingTempService bookingTempService;

    @Autowired
    private MatchingHandler matchingHandler;

    @Scheduled(cron = "0 */10 * * * *")
    @Async
    public void checkBooking() {
        log.info("method checkBooking");
        List<Booking> bookingList = bookingService.findAll()
                .stream()
                .filter(booking -> booking.getRemindAt().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (!bookingList.isEmpty()) onExpireTimeToConfirm(bookingList);
        else log.debug("No bookings to handle");
    }

    private void onExpireTimeToConfirm(List<Booking> bookingList) {
        log.info("method onExpireTimeToConfirm. bookings quantity" + bookingList.size());
        for (Booking booking : bookingList) {
            if (booking.getRemindersQuantity() < 2) {
                bookingService.incrementRemindsQuantityAndRemindTime(booking);
                matchingHandler.sendBookingAnnouncementMessage(booking);
            } else {
                log.debug("send announcement two times");
                matchingHandler.onDenyBooking(booking.getId());
//                bookingService.deleteBooking(booking);
            }
        }
    }

//    @Scheduled (cron = "0 0 3 * * *")
//    @Async
//    private void flushBookingCash() {
////        bookingTempService.flushExpired();
//    }
}
