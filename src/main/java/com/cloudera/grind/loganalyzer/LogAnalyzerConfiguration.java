package com.cloudera.grind.loganalyzer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogAnalyzerConfiguration {

    public LogAnalyzerConfiguration(
            @Value("${dburl}")
                    String dbUrl,
            @Value("${dbuser}")
                    String dbUser,
            @Value("${dbpass}")
                    String dbPass,
            @Value("${grindurl}")
                    String grindUrl,
            @Value("${report}")
                    String report
    ) {
        if (dbUrl.trim().length() == 0 || dbUser.trim().length() == 0 || (grindUrl.trim().length() == 0
        && report.trim().length() == 0) || (grindUrl.trim().length() != 0 && report.trim().length() != 0)) {
            System.out.println("Usage: --dburl=<DB URL> --dbuser=<DB user> --dbpass=<DB password> " +
                "<[--grindurl=<URL to the Grind online job result list>] | " +
                "[--report=<path to surefire report>]>");
            System.exit(1);
        }
    }
}
