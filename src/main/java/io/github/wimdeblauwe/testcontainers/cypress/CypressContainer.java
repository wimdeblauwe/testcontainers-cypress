package io.github.wimdeblauwe.testcontainers.cypress;

import com.github.dockerjava.api.model.Bind;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class CypressContainer extends GenericContainer<CypressContainer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CypressContainer.class);

    private static final String CYPRESS_IMAGE = "cypress/included";
    private static final String CYPRESS_VERSION = "4.5.0";

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_BASE_URL = "http://host.testcontainers.internal";
    private static final String DEFAULT_URL = DEFAULT_BASE_URL + ":" + DEFAULT_PORT;
    private static final String DEFAULT_CLASSPATH_RESOURCE_PATH = "e2e";
    private static final Duration DEFAULT_MAX_TOTAL_TEST_DURATION = Duration.ofMinutes(10);
    private static final GatherTestResultsStrategy DEFAULT_GATHER_TEST_RESULTS_STRATEGY = new MochawesomeGatherTestResultsStrategy();
    private static final boolean DEFAULT_AUTO_CLEAN_REPORTS = true;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private String baseUrl = DEFAULT_URL;
    private String browser;
    private String spec;
    private String classpathResourcePath = DEFAULT_CLASSPATH_RESOURCE_PATH;
    private Duration maximumTotalTestDuration = DEFAULT_MAX_TOTAL_TEST_DURATION;
    private GatherTestResultsStrategy gatherTestResultsStrategy = DEFAULT_GATHER_TEST_RESULTS_STRATEGY;
    private boolean autoCleanReports = DEFAULT_AUTO_CLEAN_REPORTS;

    public CypressContainer() {
        this(CYPRESS_IMAGE + ":" + CYPRESS_VERSION);
    }

    public CypressContainer(String dockerImageName) {
        super(dockerImageName);
        setWorkingDirectory("/e2e");
    }

    @Override
    protected void configure() {
        addEnv("CYPRESS_baseUrl", baseUrl);
        withClasspathResourceMapping(classpathResourcePath, "/e2e", BindMode.READ_WRITE);
        withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("bash", "-c", buildEntryPoint()));
    }

    @Override
    public void start() {
        super.start();

        CypressContainerOutputFollower follower = new CypressContainerOutputFollower(countDownLatch);
        followOutput(follower);
    }

    /**
     * Set the port where the server is running on. It will use <code>http://host.testcontainers.internal</code>
     * as hostname with the given port as the Cypress base URL.<br>
     * For a <code>SpringBootTest</code>, pass the injected <code>@LocalServerPort</code> here.
     *
     * @param port the port of the server
     * @return the current instance
     */
    public CypressContainer withLocalServerPort(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("port should be a positive integer, but was " + port);
        }
        this.baseUrl = DEFAULT_BASE_URL + ":" + port;
        return self();
    }

    /**
     * Set the full server url that will be used as base URL for Cypress.
     *
     * @param baseUrl the base URL for Cypress
     * @return the current instance
     */
    public CypressContainer withBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().length() == 0) {
            throw new IllegalArgumentException("baseUrl should not be blank");
        }
        this.baseUrl = baseUrl;
        return self();
    }

    /**
     * Sets the browser to use when running the tests.
     *
     * @param browser the name of the browser (e.g. chrome, firefox, electron, ...)
     * @return the current instance
     */
    public CypressContainer withBrowser(String browser) {
        if (browser == null || browser.trim().length() == 0) {
            throw new IllegalArgumentException("browser should not be blank");
        }
        this.browser = browser;
        return self();
    }

    /**
     * Sets the test(s) to run.
     * <p>
     * This can be a single test: <code>cypress/integration/todos.spec.js</code>
     * or multiple: <code>cypress/integration/login/**</code>
     * <p>
     * By default (meaning not calling this method), all tests are run.
     *
     * @param spec the test specification
     * @return the current instance
     */
    public CypressContainer withSpec(String spec) {
        if (spec == null || spec.trim().length() == 0) {
            throw new IllegalArgumentException("spec should not be blank");
        }

        this.spec = spec;
        return self();
    }

    /**
     * Set the relative path of where the cypress tests are (the path is the location of where the
     * <code>cypress.json</code> file is)
     * <br>
     * The default is <code>e2e</code>.
     * <br>
     * The recommended location to put the cypress tests is at <code>src/test/e2e</code> and have Maven configured
     * to use that as an extra resource path:
     * <pre>
     *   &#60;project&#62;
     *       &#60;build&#62;
     *           &#60;testResources&#62;
     *             &#60;testResource&#62;
     *                 &#60;directory&#62;src/test/resources&#60;/directory&#62;
     *             &#60;/testResource&#62;
     *             &#60;testResource&#62;
     *                 &#60;directory&#62;src/test/e2e&#60;/directory&#62;
     *                 &#60;targetPath&#62;e2e&#60;/targetPath&#62;
     *             &#60;/testResource&#62;
     *         &#60;/testResources&#62;
     *       &#60;/build&#62;
     *   &#60;/project&#62;
     * </pre>
     *
     * @param resourcePath the relative path
     * @return the current instance
     */
    public CypressContainer withClasspathResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.trim().length() == 0) {
            throw new IllegalArgumentException("resourcePath should not be blank");
        }
        classpathResourcePath = resourcePath;
        return self();
    }

    /**
     * Set the maximum timeout for running the Cypress tests. If the tests are not all finished within
     * that time, a {@link TimeoutException} will be thrown from the {@link #getTestResults()} method.
     * <br>
     * The default duration is 10 minutes.
     *
     * @param duration the maximum duration
     * @return the current instance
     */
    public CypressContainer withMaximumTotalTestDuration(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("duration should not be null");
        }
        maximumTotalTestDuration = duration;
        return self();
    }

    /**
     * Set the {@link GatherTestResultsStrategy} object that should be used for gathering information
     * on the Cypress tests results.
     *
     * @param strategy the {@link GatherTestResultsStrategy} instance
     * @return the current instance
     */
    public CypressContainer withGatherTestResultsStrategy(GatherTestResultsStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy should not be null");
        }
        gatherTestResultsStrategy = strategy;
        return self();
    }

    /**
     * Set the path (relative to the root of the project) where the Mochawesome reports are put.
     *
     * @param path the relative path
     * @return the current instance
     */
    public CypressContainer withMochawesomeReportsAt(Path path) {
        return withGatherTestResultsStrategy(new MochawesomeGatherTestResultsStrategy(path));
    }

    /**
     * Set if the Cypress test reports should be automatically cleaned before each run or not.
     * <br>
     * The default is <code>true</code>.
     *
     * @param autoCleanReports true if the report directory should be deleted, false otherwise.
     * @return the current instance
     */
    public CypressContainer withAutoCleanReports(boolean autoCleanReports) {
        this.autoCleanReports = autoCleanReports;
        return self();
    }

    /**
     * Waits until the Cypress tests are done and returns the results of the tests.
     *
     * @return the Cypress test results
     * @throws InterruptedException When the current thread was interrupted waiting on the Cypress tests to finish
     * @throws TimeoutException     When the tests did not finished within the configured {@link #withMaximumTotalTestDuration(Duration) maximumTotalTestDuration}
     * @throws IOException          When there was a problem parsing the Cypress test reports
     */
    public CypressTestResults getTestResults() throws InterruptedException, TimeoutException, IOException {
        boolean success = countDownLatch.await(maximumTotalTestDuration.toMillis(), TimeUnit.MILLISECONDS);
        if (success) {

            CypressTestResults results = gatherTestResultsStrategy.gatherTestResults();
            LOGGER.info("{}", results);
            if (results.getNumberOfFailingTests() > 0) {
                LOGGER.warn("There was a failure running the Cypress tests!\n\n{}", results);
            }

            return results;
        } else {
            LOGGER.warn("Cypress tests did not finish within {} duration", maximumTotalTestDuration);
            throw new TimeoutException(String.format("Cypress tests did not finish within %s duration", maximumTotalTestDuration));
        }
    }

    @NotNull
    private String buildCypressRunArguments() {
        StringBuilder builder = new StringBuilder();
        builder.append("--headless");
        if (browser != null) {
            builder.append(" --browser ")
                   .append(browser);
        }
        if (spec != null) {
            builder.append(" --spec \"")
                   .append(spec)
                   .append('\"');
        }
        return builder.toString();
    }

    @NotNull
    private String buildEntryPoint() {
        StringBuilder builder = new StringBuilder();
        if (autoCleanReports) {
            String reportsPathInContainer = getReportsPathInContainer();
            if( reportsPathInContainer.equals("/")) {
                throw new IllegalArgumentException("Reports path was /, not allowing to delete everything");
            }
            LOGGER.debug("Removing reports from {}", reportsPathInContainer);
            builder.append("rm -rf ")
                   .append(reportsPathInContainer)
                   .append(" && ");
        }
        builder.append("npm install && ");
        builder.append("cypress run ")
               .append(buildCypressRunArguments());
        return builder.toString();
    }

    @NotNull
    private String getReportsPathInContainer() {
        String pathOnHost = gatherTestResultsStrategy.getReportsPath().toAbsolutePath().toString();
        String pathInContainer = null;
        List<Bind> binds = getBinds();
        for (Bind bind : binds) {
            String path = bind.getPath();
            if(pathOnHost.startsWith(path)) {
                pathInContainer = pathOnHost.substring(path.length());
            }
        }

        if( pathInContainer == null ) {
            throw new IllegalArgumentException("Could not find matching container path in the binds: " + binds);
        }
        return pathInContainer;
    }

    private static class CypressContainerOutputFollower implements Consumer<OutputFrame> {

        private final CountDownLatch countDownLatch;

        CypressContainerOutputFollower(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void accept(OutputFrame outputFrame) {
            String logLine = StringUtils.strip(outputFrame.getUtf8String());
            LOGGER.debug(logLine);
            if (logLine.contains("Run Finished")) {
                countDownLatch.countDown();
            }
        }
    }
}
