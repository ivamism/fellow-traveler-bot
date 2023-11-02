package by.ivam.fellowtravelerbot;

import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
@Log4j
public class Extractor {
    public static final int INDEX_ZERO = 0;
    public static final int INDEX_ONE = 1;
    public static final int INDEX_TWO = 2;
    public static final String REGEX_COLON = ":";

    public static String extractParameter(String statusString, int parameterNumber) {
        String extraction = "";
        try {
            extraction = statusString.split(REGEX_COLON)[parameterNumber];
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return extraction;
    }

    public static String extractProcess(String s) {
        return extractParameter(s, INDEX_ZERO);
    }

    public static int extractId(String s, int parameterNumber) {
        int id = -1;
        try {
            id = Integer.parseInt(extractParameter(s, parameterNumber));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return id;
    }
}
