package by.ivam.fellowtravelerbot.DTO;

import lombok.*;
import org.springframework.stereotype.Component;

@Data
@Component

public class CarDTO {

    private String model;
    private String color;
    private String plateNumber;
    private String commentary;

}
