package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String vendor;
    private String model;
    private String color;
    private String plateNumber;
    private String commentary;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_chat_id")
    private User user;

}
