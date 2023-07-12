package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Settlement;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)

public class DepartureLocationDTO {
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

}
