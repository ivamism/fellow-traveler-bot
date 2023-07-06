package by.ivam.fellowtravelerbot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity
public class DeletedUser {
    @Id
    private Long chatId;
//    private String firstName;
    private String userName;
    private LocalDateTime registeredAt;
    private LocalDateTime deletedAt;
}
