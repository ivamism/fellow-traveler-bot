package by.ivam.fellowtravelerbot.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("find_passenger_request")
@Accessors(chain = true)
public class FindPassRequestRedis {
    @Id
    String requestId;
    long chatId;
    @Indexed
    String direction;
    @Indexed
    LocalDateTime DepartureAt;
    int seatsQuantity;
    @TimeToLive
    long expireDuration;
}
