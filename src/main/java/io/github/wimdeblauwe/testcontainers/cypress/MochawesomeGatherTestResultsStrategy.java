package io.github.wimdeblauwe.testcontainers.cypress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MochawesomeGatherTestResultsStrategy implements GatherTestResultsStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(MochawesomeGatherTestResultsStrategy.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path jsonReportsPath;

    public MochawesomeGatherTestResultsStrategy() {
        jsonReportsPath = FileSystems.getDefault().getPath("target", "test-classes", "e2e", "cypress", "reports", "mochawesome");
    }

    public MochawesomeGatherTestResultsStrategy(Path jsonReportsPath) {
        this.jsonReportsPath = jsonReportsPath;
    }

    @Override
    public CypressTestResults gatherTestResults() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading Mochawesome report files from {}", jsonReportsPath.toAbsolutePath());
        }

        CypressTestResults results = new CypressTestResults();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(jsonReportsPath, "*.json")) {
            for (Path path : paths) {
                MochawesomeSpecRunReport specRunReport = objectMapper.readValue(path.toFile(), MochawesomeSpecRunReport.class);
                specRunReport.fillInTestResults(results);
            }

            return results;
        }
    }

    @Override
    public Path getReportsPath() {
        return jsonReportsPath;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MochawesomeSpecRunReport {
        private Stats stats;
        private List<Result> results;

        public Stats getStats() {
            return stats;
        }

        public void setStats(Stats stats) {
            this.stats = stats;
        }

        public List<Result> getResults() {
            return results;
        }

        public void setResults(List<Result> results) {
            this.results = results;
        }

        public void fillInTestResults(CypressTestResults results) {
            results.addNumberOfTests(stats.getTests());
            results.addNumberOfPassingTests(stats.getPasses());
            results.addNumberOfFailingTests(stats.getFailures());

            for (Result result : getResults()) {
                List<Suite> suites = result.getSuites();

                List<CypressTestSuite> cypressTestSuites = new ArrayList<>();
                for (Suite suite : suites) {
                    CypressTestSuite cypressTestSuite = new CypressTestSuite(suite.getTitle());
                    List<SuiteTest> tests = suite.getTests();
                    for (SuiteTest test : tests) {
                        cypressTestSuite.add(new CypressTest(test.getTitle(), !test.isFail()));
                    }

                    cypressTestSuites.add(cypressTestSuite);
                }

                results.addSuites(cypressTestSuites);
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Stats {
            private int tests;
            private int passes;
            private int failures;

            public int getTests() {
                return tests;
            }

            public void setTests(int tests) {
                this.tests = tests;
            }

            public int getPasses() {
                return passes;
            }

            public void setPasses(int passes) {
                this.passes = passes;
            }

            public int getFailures() {
                return failures;
            }

            public void setFailures(int failures) {
                this.failures = failures;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Result {
            private List<Suite> suites;

            public List<Suite> getSuites() {
                return suites;
            }

            public void setSuites(List<Suite> suites) {
                this.suites = suites;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Suite {
            private String title;
            private List<SuiteTest> tests;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public List<SuiteTest> getTests() {
                return tests;
            }

            public void setTests(List<SuiteTest> tests) {
                this.tests = tests;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class SuiteTest {
            private String title;
            private boolean fail;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public boolean isFail() {
                return fail;
            }

            public void setFail(boolean fail) {
                this.fail = fail;
            }
        }
    }
}
