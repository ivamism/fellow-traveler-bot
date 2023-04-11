package by.ivam.fellowtravelerbot.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "chatId")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(nullable = false)
    private Long chatId;
    private String firstName;

    private String lastName;

    private String userName;

    private LocalDateTime registeredAt;

    @OneToMany(cascade = {CascadeType.ALL})
    private List<Car> cars = new ArrayList<>();


}
