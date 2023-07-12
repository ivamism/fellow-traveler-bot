package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Settlement;

import java.util.List;

public interface SettlementService {
   Settlement findById();
   List<Settlement> findAll();
   Settlement addNewSettlement(String settlementName);
   Settlement updateSettlement();
   void deleteById();

}
