package persistence.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class FilePersistenceTest {

    @Test
    void save() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
        // 获取classpath路径
        String s = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.out.println("classpath => " + s );

        new FilePersistence().save();
    }
}