package by.ivam.fellowtravelerbot.model;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
public class BookingTemp {
    @Id
    @Column(name = "id")
    private String id;

    int findPassengerRequestId;

    int findRideRequestId;

    private RequestsType bookingInitiator;

    private RequestsType canceledBy;

    private LocalDateTime bookedAt;

    private LocalDateTime expireAt;

    private boolean isExpired;
}
