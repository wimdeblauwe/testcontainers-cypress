package io.github.wimdeblauwe.testcontainers.cypress;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
        assertThat(cypressTestResults.getNumberOfTests()).isEqualTo(61);
        assertThat(cypressTestResults.getNumberOfPassingTests()).isEqualTo(57);
        assertThat(cypressTestResults.getNumberOfFailingTests()).isEqualTo(4);
        assertThat(cypressTestResults.getSuites()).hasSize(20);
        Optional<List<CypressTest>> tests = cypressTestResults.getSuites().stream()
                                                              .filter(cypressTestSuite -> cypressTestSuite.getTitle().equals("Verify Email Address"))
                                                              .findAny()
                                                              .map(CypressTestSuite::getTests);
        assertThat(tests).isPresent()
                         .get(list(CypressTest.class))
                         .hasSize(2)
                         .extracting(CypressTest::getDescription,
                                 CypressTest::isSuccess,
                                 CypressTest::getErrorMessage,
                                 CypressTest::getStackTrace)
                         .contains(tuple("should show error message if no code in url", true, null, null),
                                   tuple("should show error message if code is unknown", true, null, null));
    }

    @Test
    void shouldSaveErrorMessageAndStackTraceWhenTestIsFailed() throws IOException {
        URL url = getClass().getResource("mochawesome");
        MochawesomeGatherTestResultsStrategy strategy = new MochawesomeGatherTestResultsStrategy(Paths.get(URI.create(url.toString())));
        CypressTestResults cypressTestResults = strategy.gatherTestResults();

        Optional<List<CypressTest>> tests = cypressTestResults.getSuites().stream()
                .filter(cypressTestSuite -> cypressTestSuite.getTitle().equals("First File"))
                .findAny()
                .map(CypressTestSuite::getTests);
        assertThat(tests).isPresent()
                .get(list(CypressTest.class))
                .hasSize(1)
                .extracting(CypressTest::getDescription,
                        CypressTest::isSuccess,
                        CypressTest::getErrorMessage,
                        CypressTest::getStackTrace)
                .contains(tuple("Log in", false,
                        "AssertionError: Timed out retrying after 4000ms: Expected to find element: `.welcome-message`, but never found it.",
                        "AssertionError: Timed out retrying after 4000ms: Expected to find element: `.welcome-message`, but never found it.\n    at Context.eval (http://localhost/__cypress/tests?p=cypress/integration/firstFile.js:170:32)"));
    }
}
