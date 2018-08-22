package com.cloudera.grind.loganalyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GrindOutputManager {

    public static int GRIND_URL_TIMEOUT = 60 * 1000;

    private String taskSelector;
    private String descriptionSelector;
    private String surefireUrlSelector;
    private String taskIdSelector;
    private String taskSuccessfulClass;

    public GrindOutputManager(
            @Value("${analyzer.grind.output.taskSelector}") String taskSelector,
            @Value("${analyzer.grind.output.descriptionSelector}")String descriptionSelector,
            @Value("${analyzer.grind.output.surefireUrlSelector}")String surefireUrlSelector,
            @Value("${analyzer.grind.output.taskIdSelector}")String taskIdSelector,
            @Value("${analyzer.grind.output.taskSuccessfulClass}")String taskSuccessfulClass) {
        this.taskSelector = taskSelector;
        this.descriptionSelector = descriptionSelector;
        this.surefireUrlSelector = surefireUrlSelector;
        this.taskIdSelector = taskIdSelector;
        this.taskSuccessfulClass = taskSuccessfulClass;
    }

    public List<GrindTask> parseGrindOutput(URL grindUrl) throws IOException {
        Document grindPage = Jsoup.connect(grindUrl.toString()).maxBodySize(0).timeout(GRIND_URL_TIMEOUT).get();
        return grindPage
                .select(taskSelector)
                .stream().map(element -> new GrindTask(
                        element.select(descriptionSelector).html(),
                        element.select(surefireUrlSelector).attr("href"),
                        element.select(taskIdSelector).html(),
                        taskSuccessfulClass.equals(element.attr("class"))
                ))
                .collect(Collectors.toList());
    }
}
