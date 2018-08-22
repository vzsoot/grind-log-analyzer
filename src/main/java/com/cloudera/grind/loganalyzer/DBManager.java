package com.cloudera.grind.loganalyzer;

import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DBManager {

    public DBManager(
            JdbcTemplate jdbcTemplate,
            @Value("${analyzer.database.initSchema}") String initSchema,
            @Value("${analyzer.database.schema}") String schema,
            @Value("${analyzer.database.insertEventStatement}") String insertEventStatement,
            @Value("${analyzer.database.insertTaskStatement}") String insertTaskStatement,
            @Value("${analyzer.database.insertTestCaseStatement}") String insertTestCaseStatement
            ) {
        this.jdbcTemplate = jdbcTemplate;
        this.initSchema = initSchema;
        this.schema = schema;
        this.insertEventStatement = insertEventStatement;
        this.insertTaskStatement = insertTaskStatement;
        this.insertTestCaseStatement = insertTestCaseStatement;
    }

    private JdbcTemplate jdbcTemplate;

    private String schema;
    private String initSchema;
    private String insertEventStatement;
    private String insertTaskStatement;
    private String insertTestCaseStatement;

    public void createSchema() {
        if (initSchema.trim().length() > 0) {
            jdbcTemplate.execute(initSchema);
        }
        if (schema.trim().length() > 0) {
            jdbcTemplate.execute(schema);
        }
    }

    public String insertTask(GrindTask task) {
        jdbcTemplate.update(insertTaskStatement,
                task.getDescription(), task.getSurefireUrl(), task.getTaskId(), task.getSuccess());

        return task.getTaskId();
    }

    public Integer insertTestCase(SurefireParser.ReportTestCase testCase, String taskId) {

        PreparedStatementCreator testCaseStatement = connection -> {
            PreparedStatement statement = connection.prepareStatement(insertTestCaseStatement, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, taskId);
            statement.setString(2, testCase.getMethodName());
            statement.setString(3, testCase.getClassName());
            statement.setString(4, testCase.getMessage());
            statement.setInt(5, testCase.getSuccess() ? 1 : 0);
            return statement;
        };

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(testCaseStatement, keyHolder);

        return (Integer)keyHolder.getKeys().get("id");
    }

    public void insertEvents(List<LoggingEvent> events, Integer testCaseId) {
        List<Object[]> eventRecords = events.stream()
                .map(event ->
                    new Object[] {
                        testCaseId,
                        event.timeStamp, event.timeStamp % 1000, event.getLoggerName(), event.getLevel().toString(),
                        event.getThreadName(), "", "", 0, "", event.getMessage()
                    }
                )
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(this.insertEventStatement, eventRecords);
    }
}
