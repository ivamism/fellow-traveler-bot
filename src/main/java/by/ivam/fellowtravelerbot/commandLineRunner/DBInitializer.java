package by.ivam.fellowtravelerbot.commandLineRunner;

import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.repository.SettlementRepository;
import by.ivam.fellowtravelerbot.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Component
@Order(1)
@Log4j2
public class DBInitializer implements CommandLineRunner {
    @Autowired
    UserRepository userRepository;
    @Autowired
    SettlementRepository settlementRepository;

    private final String KOROLEVO = "Королево";
    private final String MINSK = "Минск";

    @Override
    public void run(String... args) throws Exception {
        log.info("DBInitializer:");
        if (settlementRepository.count() == 0) {
            log.info("Table Settlement in DB is empty. Save settlments Korolevo & Minsk");
            settlementRepository.save(new Settlement().setName(KOROLEVO));
            settlementRepository.save(new Settlement().setName(MINSK));
        } else log.info("no conditions to save default settlements");
        if (userRepository.count() == 0) {
            log.info("Table Users is empty. Save default MasterAdminUser");
            User masterAdmin = new User();
            masterAdmin.setChatId(785703113L)
                    .setFirstName("Ivan")
                    .setUserName("ivam_IM")
                    .setResidence(settlementRepository.findByName(KOROLEVO).orElseThrow(() -> new NoSuchElementException()))
                    .setAdmin(true)
                    .setRegisteredAt(LocalDateTime.now());
            userRepository.save(masterAdmin);
        } else log.info("no conditions to save default MasterAdminUser");
    }
}
