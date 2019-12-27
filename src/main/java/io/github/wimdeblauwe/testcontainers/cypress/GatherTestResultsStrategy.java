package io.github.wimdeblauwe.testcontainers.cypress;

import java.io.IOException;

public interface GatherTestResultsStrategy {
    CypressTestResults gatherTestResults() throws IOException;

    void cleanReports() throws IOException;
}
