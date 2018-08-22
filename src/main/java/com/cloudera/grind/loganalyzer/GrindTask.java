package com.cloudera.grind.loganalyzer;

public class GrindTask {
  private String description;
  private String surefireUrl;
  private String taskId;
  private Boolean success;

  public GrindTask(String description, String surefireUrl, String taskId, Boolean success) {
    this.description = description;
    this.surefireUrl = surefireUrl;
    this.taskId = taskId;
    this.success = success;
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

  public Integer getSuccess() {
    return success == null ? -1 : success ? 1 : 0;
  }

  @Override
  public String toString() {
    return "GrindTask{" +
        "description='" + description + '\'' +
        ", surefireUrl='" + surefireUrl + '\'' +
        ", taskId='" + taskId + '\'' +
        ", success='" + success + '\'' +
        '}';
  }
}