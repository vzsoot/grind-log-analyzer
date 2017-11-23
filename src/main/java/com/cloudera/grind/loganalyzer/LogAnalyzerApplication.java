package com.cloudera.grind.loganalyzer;

import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
public class LogAnalyzerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(LogAnalyzerApplication.class, args);
    }

    @Value("${logFormat}")
    String logFormat;

    @Value("${grindurl}")
    String grindUrl;

    @Autowired
    DBManager dbManager;

    @Autowired
    GrindOutputManager grindOutputManager;

    @Autowired
    GrindArtifactManager grindArtifactManager;

    @Autowired
    SurefireParser surefireParser;

    @Override
    public void run(String... args) throws Exception {
        try {
            dbManager.createSchema();

            grindOutputManager.parseGrindOutput(new URL(grindUrl))
                .forEach(grindTask -> {
                    dbManager.insertTask(grindTask);

                    try {
                        grindArtifactManager.fetchSurefireReport(new URL(grindTask.getSurefireUrl()), reportStream -> {
                            try {
                                surefireParser.parseSurefireReport(reportStream).forEach(testCase -> {
                                    Integer testCaseId = dbManager.insertTestCase(testCase, grindTask.getTaskId());

                                    Log4jParser log4jParser = new Log4jParser();
                                    log4jParser.setLogFormat(logFormat);

                                    List<LoggingEvent> events = new LinkedList<>();
                                    log4jParser.setReceiver(events::add);
                                    try {
                                        log4jParser.parse(
                                                new BufferedReader(
                                                        new InputStreamReader(
                                                                new ByteArrayInputStream(testCase.getSystemOut()))));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    dbManager.insertEvents(events, testCaseId);
                                });
                            } catch (IOException | SAXException | ParserConfigurationException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
