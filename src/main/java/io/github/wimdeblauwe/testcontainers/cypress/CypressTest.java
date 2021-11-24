package io.github.wimdeblauwe.testcontainers.cypress;

public class CypressTest {
    private String description;
    private boolean success;
    private String errorMessage;
    private String stackTrace;

    public CypressTest(String description, boolean success) {
        this.description = description;
        this.success = success;
    }

    public CypressTest(String description, boolean success, String errorMessage, String stackTrace) {
        this.description = description;
        this.success = success;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}
