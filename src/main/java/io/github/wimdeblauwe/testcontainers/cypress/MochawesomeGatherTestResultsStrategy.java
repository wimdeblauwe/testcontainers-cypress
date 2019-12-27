package io.github.wimdeblauwe.testcontainers.cypress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MochawesomeGatherTestResultsStrategy implements GatherTestResultsStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(MochawesomeGatherTestResultsStrategy.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path xmlReportsPath;

    public MochawesomeGatherTestResultsStrategy() {
        xmlReportsPath = FileSystems.getDefault().getPath("target", "test-classes", "e2e", "cypress", "reports", "mochawesome");
    }

    public MochawesomeGatherTestResultsStrategy(Path xmlReportsPath) {
        this.xmlReportsPath = xmlReportsPath;
    }

    @Override
    public CypressTestResults gatherTestResults() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading Mochawesome report files from {}", xmlReportsPath.toAbsolutePath());
        }

        CypressTestResults results = new CypressTestResults();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(xmlReportsPath)) {
            for (Path path : paths) {
                MochawesomeSpecRunReport specRunReport = objectMapper.readValue(path.toFile(), MochawesomeSpecRunReport.class);
                specRunReport.updateTotals(results);
            }

            return results;
        }
    }

    @Override
    public void cleanReports() throws IOException {
        Files.walkFileTree(xmlReportsPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MochawesomeSpecRunReport {
        private Stats stats;

        public Stats getStats() {
            return stats;
        }

        public void setStats(Stats stats) {
            this.stats = stats;
        }

        public void updateTotals(CypressTestResults results) {
            results.addNumberOfTests(stats.getTests());
            results.addNumberOfPassingTests(stats.getPasses());
            results.addNumberOfFailingTests(stats.getFailures());
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
    }
}
