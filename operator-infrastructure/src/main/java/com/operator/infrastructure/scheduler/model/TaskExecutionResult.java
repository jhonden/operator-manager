package com.operator.infrastructure.scheduler.model;

/**
 * Task Execution Result
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public class TaskExecutionResult {

    private boolean success;
    private String outputData;
    private String errorMessage;
    private Integer exitCode;

    public TaskExecutionResult() {
    }

    public TaskExecutionResult(boolean success, String outputData, String errorMessage, Integer exitCode) {
        this.success = success;
        this.outputData = outputData;
        this.errorMessage = errorMessage;
        this.exitCode = exitCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutputData() {
        return outputData;
    }

    public void setOutputData(String outputData) {
        this.outputData = outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Create successful result
     */
    public static TaskExecutionResult success(String outputData) {
        return new TaskExecutionResult(true, outputData, null, 0);
    }

    /**
     * Create failure result
     */
    public static TaskExecutionResult failure(String errorMessage) {
        return new TaskExecutionResult(false, null, errorMessage, 1);
    }
}
