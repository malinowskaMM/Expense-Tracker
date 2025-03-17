package pl.lodz.p.it.expenseTracker.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoggerService {

    private final Logger logger;

    public LoggerService() {
        this.logger = LoggerFactory.getLogger("ExpenseTracker");
    }

    public void log(String message, String service, LoggerServiceLevel level) {
        String loggerMessage = LocalDateTime.now() + " serviceName: " + service + " " + message;
        switch (level) {
            case DEBUG -> logger.debug(loggerMessage);
            case INFO -> logger.info(loggerMessage);
            case WARN -> logger.warn(loggerMessage);
            case ERROR -> logger.error(loggerMessage);
            default -> throw new IllegalArgumentException("Invalid log level: " + level);
        }
    }

    public enum LoggerServiceLevel {
        DEBUG, INFO, WARN, ERROR
    }
}
