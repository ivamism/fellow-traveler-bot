package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
public class DepartureLocation {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    @ManyToOne()
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

}
