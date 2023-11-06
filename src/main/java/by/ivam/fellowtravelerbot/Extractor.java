package by.ivam.fellowtravelerbot;

import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
@Log4j
public class Extractor {
    private   final int INDEX_ZERO = 0;
    private   final int INDEX_ONE = 1;
    private   final int INDEX_TWO = 2;
    private   final String REGEX_COLON = ":";

    public String extractParameter(String statusString, int parameterNumber) {
        String extraction = "";
        try {
            extraction = statusString.split(REGEX_COLON)[parameterNumber];
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return extraction;
    }

    public String extractProcess(String s) {
        return extractParameter(s, INDEX_ZERO);
    }

    public int extractId(String s, int parameterNumber) {
        int id = -1;
        try {
            id = Integer.parseInt(extractParameter(s, parameterNumber));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return id;
    }
}
