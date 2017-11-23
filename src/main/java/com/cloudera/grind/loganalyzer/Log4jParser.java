package com.cloudera.grind.loganalyzer;

import org.apache.log4j.receivers.varia.LogFilePatternReceiver;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;

public class Log4jParser extends LogFilePatternReceiver {

    public void parse(BufferedReader reader) throws IOException {
        this.setHost("local");
        this.setPath("local");
        this.initialize();
        this.createPattern();
        this.process(reader);
    }

    private Consumer<LoggingEvent> receiver;

    public void setReceiver(Consumer<LoggingEvent> receiver) {
        this.receiver = receiver;
    }

    @Override
    public void doPost(LoggingEvent event) {
        this.receiver.accept(event);
    }
}
