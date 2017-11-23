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
                    String grindUrl
    ) {
        if (dbUrl.trim().length() == 0 || dbUser.trim().length() == 0 || dbPass.trim().length() == 0) {
            System.out.println("Usage: --dburl=<DB URL> --dbuser=<DB user> --dbpass=<DB password> " +
                    "--grindUrl=<URL to the Grind online job result list>");
            System.exit(1);
        }
    }
}
