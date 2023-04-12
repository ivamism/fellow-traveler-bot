package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String model;
    private String color;
    private String commentary;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_chat_id")
    private User user;

}
