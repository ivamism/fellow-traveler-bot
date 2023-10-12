package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
public class Car {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String model;
    private String color;
    private String plateNumber;
    private String commentary;

    @ManyToOne
    @JoinColumn(name = "user.chat_id")
    private User user;
}
