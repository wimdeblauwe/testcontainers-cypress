package io.github.wimdeblauwe.testcontainers.cypress;

import java.io.IOException;
import java.nio.file.Path;

public interface GatherTestResultsStrategy {
    CypressTestResults gatherTestResults() throws IOException;

    Path getReportsPath();
}
