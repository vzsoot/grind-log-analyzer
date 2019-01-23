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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class LogAnalyzerApplication implements CommandLineRunner {

    private static Logger LOG = Logger.getLogger(LogAnalyzerApplication.class.getName());

    @Autowired
    public LogAnalyzerApplication(DBManager dbManager, GrindOutputManager grindOutputManager, GrindArtifactManager grindArtifactManager, SurefireParser surefireParser) {
        this.dbManager = dbManager;
        this.grindOutputManager = grindOutputManager;
        this.grindArtifactManager = grindArtifactManager;
        this.surefireParser = surefireParser;
    }

    public static void main(String[] args) {
        SpringApplication.run(LogAnalyzerApplication.class, args);
    }

    @Value("${logFormat}")
    String logFormat;

    @Value("${grindurl}")
    String grindUrl;

    @Value("${report}")
    String report;

    private final DBManager dbManager;
    private final GrindOutputManager grindOutputManager;
    private final GrindArtifactManager grindArtifactManager;
    private final SurefireParser surefireParser;

    void doOnlineRunParse(String url) {
        try {
            List<GrindTask> grindTasks = grindOutputManager.parseGrindOutput(new URL(url));
            LOG.log(Level.INFO, "Found " + grindTasks.size() + " failed task runs.");

            AtomicInteger parseCount = new AtomicInteger();

            grindTasks.parallelStream().forEach(grindTask -> {
                dbManager.insertTask(grindTask);

                LOG.log(Level.INFO,
                    String.format(
                        "Parsing %1$" + Integer.toString(grindTasks.size()).length() + "d / %2$d failed task runs.",
                        parseCount.incrementAndGet(), grindTasks.size()));

                try {
                    grindArtifactManager.fetchSurefireReport(
                        new URL(grindTask.getSurefireUrl()),
                        reportStream -> doSurefireReportParse(reportStream, grindTask.getTaskId())
                    );
                } catch (Throwable e) {
                    LOG.log(Level.SEVERE, e.getMessage() + " " + grindTask, e);
                }
            });

        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    void doSurefireReportParse(InputStream reportStream, String taskId) {
        try {
            surefireParser.parseSurefireReport(reportStream).forEach(testCase -> {
                Integer testCaseId = dbManager.insertTestCase(testCase, taskId);

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
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }

                dbManager.insertEvents(events, testCaseId);
            });
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void run(String... args) {
        dbManager.createSchema();

        if (grindUrl.trim().length()!=0) {
            doOnlineRunParse(grindUrl.trim());
        } else if (report.trim().length()!=0) {
            String taskId = "report_" + UUID.randomUUID().toString();

            LOG.log(Level.INFO, "Task ID: " + taskId);

            File reportFile = new File(report.trim());

            GrindTask surefireTask = new GrindTask(
                reportFile.getName(), report.trim(), taskId
            );
            dbManager.insertTask(surefireTask);

            try {
                doSurefireReportParse(new FileInputStream(report.trim()), surefireTask.getTaskId());
            } catch (FileNotFoundException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
