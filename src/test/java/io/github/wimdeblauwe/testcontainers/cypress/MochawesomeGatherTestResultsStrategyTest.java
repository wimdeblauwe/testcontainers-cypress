package io.github.wimdeblauwe.testcontainers.cypress;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class MochawesomeGatherTestResultsStrategyTest {

    @Test
    void testGatherTestResults() throws IOException {
        URL url = getClass().getResource("mochawesome");
        MochawesomeGatherTestResultsStrategy strategy = new MochawesomeGatherTestResultsStrategy(Paths.get(URI.create(url.toString())));
        CypressTestResults cypressTestResults = strategy.gatherTestResults();
        assertThat(cypressTestResults).isNotNull();
        assertThat(cypressTestResults.getNumberOfTests()).isEqualTo(60);
        assertThat(cypressTestResults.getNumberOfPassingTests()).isEqualTo(57);
        assertThat(cypressTestResults.getNumberOfFailingTests()).isEqualTo(3);
    }

    @Test
    void testCleanReportsIfDirectoryDoesNotExist(@TempDir Path path ) throws IOException {
        MochawesomeGatherTestResultsStrategy strategy = new MochawesomeGatherTestResultsStrategy(path.resolve("non-existing"));
        strategy.cleanReports();
    }
}
