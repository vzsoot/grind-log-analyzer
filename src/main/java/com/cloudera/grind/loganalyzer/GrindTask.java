package com.cloudera.grind.loganalyzer;

public class GrindTask {
  private String description;
  private String surefireUrl;
  private String taskId;

  public GrindTask(String description, String surefireUrl, String taskId) {
    this.description = description;
    this.surefireUrl = surefireUrl;
    this.taskId = taskId;
  }

  public String getDescription() {
    return description;
  }

  public String getSurefireUrl() {
    return surefireUrl;
  }

  public String getTaskId() {
    return taskId;
  }

  @Override
  public String toString() {
    return "GrindTask{" +
        "description='" + description + '\'' +
        ", surefireUrl='" + surefireUrl + '\'' +
        ", taskId='" + taskId + '\'' +
        '}';
  }
}