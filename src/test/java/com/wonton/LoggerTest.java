package com.wonton;

import com.wonton.logger.Logger;
import org.junit.jupiter.api.Test;

public class LoggerTest {

    @Test
    public void testLogger() {
        Logger.debug("This is a debug message");
        Logger.info("This is an info message");
        Logger.primary("This is a primary message");
        Logger.warn("This is a warning message");
        Logger.error("This is an error message");
        Logger.success("This is a success message");
    }

}
