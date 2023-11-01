package by.ivam.fellowtravelerbot.redis.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class Dto {
    int id;
    String direction;
    LocalDateTime DepartureAt;
    int seatsQuantity;
//    long duration;
}
