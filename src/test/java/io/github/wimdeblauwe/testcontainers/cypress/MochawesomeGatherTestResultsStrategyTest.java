package io.github.wimdeblauwe.testcontainers.cypress;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

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
        assertThat(cypressTestResults.getSuites()).hasSize(19);
        Optional<List<CypressTest>> tests = cypressTestResults.getSuites().stream()
                                                              .filter(cypressTestSuite -> cypressTestSuite.getTitle().equals("Verify Email Address"))
                                                              .findAny()
                                                              .map(CypressTestSuite::getTests);
        assertThat(tests).isPresent()
                         .get(list(CypressTest.class))
                         .hasSize(2)
                         .extracting(CypressTest::getDescription,
                                     CypressTest::isSuccess)
                         .contains(tuple("should show error message if no code in url", true),
                                   tuple("should show error message if code is unknown", true));
    }

    @Test
    void testCleanReportsIfDirectoryDoesNotExist(@TempDir Path path) throws IOException {
        MochawesomeGatherTestResultsStrategy strategy = new MochawesomeGatherTestResultsStrategy(path.resolve("non-existing"));
        strategy.cleanReports();
    }
}
