package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Settlement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)

public class LocationDTO {
    private String name;
    private Settlement settlement;
}
