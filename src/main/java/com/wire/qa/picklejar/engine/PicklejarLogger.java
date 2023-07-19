package com.wire.qa.picklejar.engine;

import java.util.Arrays;
import java.util.Date;
import java.util.logging.*;

public class PicklejarLogger {

    public static void configureLogging(PicklejarConfiguration configuration) {
        Logger logger = Logger.getLogger("com.wire.qa.picklejar.engine");
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private final String format = configuration.getLoggingFormat();

            @Override
            public synchronized String format(LogRecord record) {
                return String.format(format,
                        new Date(record.getMillis()),
                        record.getLevel().getLocalizedName(),
                        record.getLoggerName(),
                        record.getMessage()
                );
            }
        });
        removeAllHandler(logger);
        logger.addHandler(handler);
        Logger launcherLogger = Logger.getLogger("org.junit.platform.launcher");
        removeAllHandler(launcherLogger);
        launcherLogger.addHandler(handler);
    }

    private static void removeAllHandler(Logger logger) {
        Arrays.stream(logger.getHandlers()).forEach(logger::removeHandler);
    }

}
