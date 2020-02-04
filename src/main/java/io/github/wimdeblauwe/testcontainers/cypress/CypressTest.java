package io.github.wimdeblauwe.testcontainers.cypress;

public class CypressTest {
    private String description;
    private boolean success;

    public CypressTest(String description, boolean success) {
        this.description = description;
        this.success = success;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccess() {
        return success;
    }
}
