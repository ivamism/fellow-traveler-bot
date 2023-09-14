package by.ivam.fellowtravelerbot.servise.handler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminHandlerTest {
    @Autowired
    AdminHandler handler;

    @Test
    void departureLocationButtonsAttributesListCreator() {
        List<Pair<String, String>> data = handler.locationButtonsAttributesListCreator("data", 2);
        assertNotNull(data);
    }
}