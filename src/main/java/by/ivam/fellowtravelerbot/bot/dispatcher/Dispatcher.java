package by.ivam.fellowtravelerbot.bot.dispatcher;

public interface Dispatcher {
    default String getHandler(String s) {
        return s.split("-")[0];
    }
    default String getProcess(String s) {
        return s.split("-")[1];
    }
}
