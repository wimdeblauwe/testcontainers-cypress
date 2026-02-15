package io.github.wimdeblauwe.testcontainers.cypress;

import com.github.dockerjava.api.command.CreateContainerCmd;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import org.testcontainers.core.CreateContainerCmdModifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CypressContainerTest {

    @Test
    void testDefaultDockerImage() {
        try (CypressContainer container = new CypressContainer()) {
            container.configure();
            assertThat(container.getDockerImageName()).isEqualTo("cypress/included:15.10.0");
            assertThat(container.getWorkingDirectory()).isEqualTo("/e2e");
        }
    }

    @Test
    void testCustomDockerImage() {
        try (CypressContainer container = new CypressContainer("cypress/included:13.14.0")) {
            container.configure();
            assertThat(container.getDockerImageName()).isEqualTo("cypress/included:13.14.0");
            assertThat(container.getWorkingDirectory()).isEqualTo("/e2e");
        }
    }

    @Test
    void testDefaultBaseUrl() {
        try (CypressContainer container = new CypressContainer()) {
            container.configure();
            assertThat(container.getEnvMap()).containsEntry("CYPRESS_baseUrl","http://host.testcontainers.internal:8080");
        }
    }

    @Test
    void testWithLocalServerPort() {
        try (CypressContainer container = new CypressContainer()
                .withLocalServerPort(1313)) {
            container.configure();
            assertThat(container.getEnvMap()).containsEntry("CYPRESS_baseUrl","http://host.testcontainers.internal:1313");
        }
    }

    @Test
    void testWithLocalServerPortIfPortIsNegative() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer().withLocalServerPort(-1313));
    }

    @Test
    void testWithBaseUrl() {
        try (CypressContainer container = new CypressContainer()
                .withBaseUrl("https://www.wimdeblauwe.com")) {
            container.configure();
            assertThat(container.getEnvMap()).containsEntry("CYPRESS_baseUrl","https://www.wimdeblauwe.com");
        }
    }

    @Test
    void testWithBaseUrlIfBaseUrlIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withBaseUrl(null));
    }

    @Test
    void testWithBaseUrlIfBaseUrlIsEmpty() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withBaseUrl(""));
    }

    @Test
    void testWithBrowser() {
        Set<CreateContainerCmdModifier> createContainerCmdModifiers;
        try (CypressContainer container = new CypressContainer()
                .withBrowser("firefox")
                .withAutoCleanReports(false)) {
            container.configure();
            createContainerCmdModifiers = container.getCreateContainerCmdModifiers();
        }
        assertThat(createContainerCmdModifiers).hasSize(1);
        CreateContainerCmdModifier createContainerCmdModifier = createContainerCmdModifiers.iterator().next();
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        createContainerCmdModifier.modify(cmd);
        verify(cmd).withEntrypoint("bash", "-c", "npm install && cypress run --headless --browser firefox");
    }

    @Test
    void testWithBrowserIfBrowserIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withBrowser(null));
    }

    @Test
    void testWithBrowserIfBrowserIsEmpty() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withBrowser(""));
    }

    @Test
    void testWithAutoCleanReports() {
        Set<CreateContainerCmdModifier> createContainerCmdModifiers;
        try (CypressContainer container = new CypressContainer()
                .withAutoCleanReports(true)) {
            container.configure();
            createContainerCmdModifiers = container.getCreateContainerCmdModifiers();
        }
        assertThat(createContainerCmdModifiers).hasSize(1);
        CreateContainerCmdModifier createContainerCmdModifier = createContainerCmdModifiers.iterator().next();
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        createContainerCmdModifier.modify(cmd);
        verify(cmd).withEntrypoint("bash",
                                   "-c",
                                   "rm -rf cypress/reports/mochawesome && npm install && cypress run --headless");
    }

    @Test
    void testWithSpec() {
        Set<CreateContainerCmdModifier> createContainerCmdModifiers;
        try (CypressContainer container = new CypressContainer()
                .withSpec("cypress/integration/todos.spec.js")
                .withAutoCleanReports(false)) {

            container.configure();
            createContainerCmdModifiers = container.getCreateContainerCmdModifiers();
        }
        assertThat(createContainerCmdModifiers).hasSize(1);
        CreateContainerCmdModifier createContainerCmdModifier = createContainerCmdModifiers.iterator().next();
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        createContainerCmdModifier.modify(cmd);
        verify(cmd).withEntrypoint("bash", "-c", "npm install && cypress run --headless --spec \"cypress/integration/todos.spec.js\"");
    }

    @Test
    void testWithSpecIfSpecIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withSpec(null));
    }

    @Test
    void testWithSpecIfSpecIsEmpty() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withSpec(""));
    }

    @Test
    void testWithClasspathResourcePath() {
        Set<CreateContainerCmdModifier> createContainerCmdModifiers;
        try (CypressContainer container = new CypressContainer()
                .withClasspathResourcePath("io")
                .withAutoCleanReports(true)
                .withMochawesomeReportsAt(Paths.get("target", "test-classes", "io", "github", "wimdeblauwe"))) {
            container.configure();
            createContainerCmdModifiers = container.getCreateContainerCmdModifiers();
        }
        assertThat(createContainerCmdModifiers).hasSize(1);
        CreateContainerCmdModifier createContainerCmdModifier = createContainerCmdModifiers.iterator().next();
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        createContainerCmdModifier.modify(cmd);
        verify(cmd).withEntrypoint("bash",
                                   "-c",
                                   "rm -rf github/wimdeblauwe && npm install && cypress run --headless");
    }

    @Test
    void testWithClasspathResourcePathIfClasspathResourcePathIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withClasspathResourcePath(null));
    }

    @Test
    void testWithClasspathResourcePathIfClasspathResourcePathIsEmpty() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CypressContainer()
                        .withClasspathResourcePath(""));
    }

    @Test
    void testWithRecord() {
        Set<CreateContainerCmdModifier> createContainerCmdModifiers;
        try (CypressContainer container = new CypressContainer()
                .withAutoCleanReports(false)
                .withRecord()) {

            container.configure();
            createContainerCmdModifiers = container.getCreateContainerCmdModifiers();
        }
        assertThat(createContainerCmdModifiers).hasSize(1);
        CreateContainerCmdModifier createContainerCmdModifier = createContainerCmdModifiers.iterator().next();
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        createContainerCmdModifier.modify(cmd);
        verify(cmd).withEntrypoint("bash", "-c", "npm install && cypress run --headless --record");
    }

    @Test
    void testWithRecordKey() {
        String recordKey = UUID.randomUUID().toString();
        Set<CreateContainerCmdModifier> createContainerCmdModifiers;
        try (CypressContainer container = new CypressContainer()
                .withAutoCleanReports(false)
                .withRecord(recordKey)) {

            container.configure();
            createContainerCmdModifiers = container.getCreateContainerCmdModifiers();
        }
        assertThat(createContainerCmdModifiers).hasSize(1);
        CreateContainerCmdModifier createContainerCmdModifier = createContainerCmdModifiers.iterator().next();
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        createContainerCmdModifier.modify(cmd);
        verify(cmd).withEntrypoint("bash", "-c", "npm install && cypress run --headless --record --key " + recordKey);
    }

    @Test
    void testWithAdditionalNpmInstallArguments() {
        Set<CreateContainerCmdModifier> createContainerCmdModifiers;
        try (CypressContainer container = new CypressContainer()
                .withNpmRunArguments( "--loglevel silent")) {
            container.configure();
            createContainerCmdModifiers = container.getCreateContainerCmdModifiers();
        }
        assertThat(createContainerCmdModifiers).hasSize(1);
        CreateContainerCmdModifier createContainerCmdModifier = createContainerCmdModifiers.iterator().next();
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        createContainerCmdModifier.modify(cmd);
        verify(cmd).withEntrypoint("bash", "-c", "rm -rf cypress/reports/mochawesome && npm install --loglevel silent && cypress run --headless");

    }
}
