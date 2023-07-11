package by.ivam.fellowtravelerbot.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long chatId;
    private String firstName;
    private String userName;
    private LocalDateTime registeredAt;
    private boolean isAdmin;
    private boolean isDeleted;
    private LocalDateTime deletedAt;
    private boolean isBlocked;
    private LocalDateTime blockedAt;

    @ManyToOne
    @JoinColumn(name = "settlement_id")
    private Settlement residence;

}
