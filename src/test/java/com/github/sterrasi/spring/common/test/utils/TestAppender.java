package com.github.sterrasi.spring.common.test.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class TestAppender extends ListAppender<ILoggingEvent> {

    private static TestAppender appender;

    public static void initialize(String loggerName){
        initialize(loggerName, Level.DEBUG);
    }
    public static void initialize(String loggerName, Level logLevel){

        appender = new TestAppender();
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());

        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.setLevel(logLevel);
        logger.addAppender(appender);

        appender.start();
    }

    public static void reset(){
        appender.list.clear();
    }

    public static boolean contains(String subject){
        return appender.list.stream()
                .anyMatch( e -> e.getFormattedMessage().contains(subject));
    }

    public static boolean contains(CharSequence sequence, Level level){
        return appender.list.stream()
                .anyMatch( e -> e.getFormattedMessage().contains(sequence) && e.getLevel() == level);
    }

    public static List<ILoggingEvent> search(CharSequence sequence){
        return appender.list.stream()
                .filter( e -> e.getFormattedMessage().contains(sequence))
                .collect(Collectors.toList());
    }

    public static List<ILoggingEvent> search(CharSequence sequence, Level level){
        return appender.list.stream()
                .filter( e -> e.getFormattedMessage().contains(sequence) && e.getLevel() == level)
                .collect(Collectors.toList());
    }
}
