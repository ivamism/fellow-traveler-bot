package by.ivam.fellowtravelerbot.DTO;

import lombok.*;
import org.springframework.stereotype.Component;

@Data
@ToString
@Component

public class CarDTO {

    private String vendor;
    private String model;
    private String color;
    private String plateNumber;
    private String commentary;

}
