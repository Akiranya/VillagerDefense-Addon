package cc.mewcraft.villagedefense.util;

import cc.mewcraft.villagedefense.VDA;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class CustomLogger {

    private final Logger logger;

    public static CustomLogger create() {
        return new CustomLogger(VDA.instance().getLogger());
    }

    public CustomLogger(Logger logger) {
        this.logger = logger;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warn(String msg) {
        logger.warning(msg);
    }

    public void warn(String msg, Throwable thrown) {
        logger.warning(msg);
        thrown.printStackTrace();
    }

    public void error(String msg) {
        logger.severe(msg);
    }

    public void error(String msg, Throwable thrown) {
        logger.severe(msg);
        thrown.printStackTrace();
    }

    public void reportException(Throwable thrown) {
        thrown.printStackTrace();
    }

}
