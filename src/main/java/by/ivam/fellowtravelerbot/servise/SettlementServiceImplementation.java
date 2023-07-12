package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.repository.SettlementRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Log4j
public class SettlementServiceImplementation implements SettlementService {
//    @Autowired
//    Settlement settlement;
    @Autowired
    SettlementRepository settlementRepository;

    @Override
    public Settlement findById() {

        return null;
    }

    @Override
    public List<Settlement> findAll() {
        return null;
    }

    @Override
    public Settlement addNewSettlement(String settlementName) {
        Settlement settlement = new Settlement();
        settlement.setName(settlementName);
        settlementRepository.save(settlement);
        log.info("Settlement " + settlement + " saved to DB");
        return settlement;
    }

    @Override
    public Settlement updateSettlement() {
        return null;
    }

    @Override
    public void deleteById() {

    }
}
