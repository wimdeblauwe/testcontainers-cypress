package io.github.wimdeblauwe.testcontainers.cypress;

public class CypressTestResults {
    int numberOfTests;
    int numberOfPassingTests;
    int numberOfFailingTests;

    public int getNumberOfTests() {
        return numberOfTests;
    }

    void setNumberOfTests(int numberOfTests) {
        this.numberOfTests = numberOfTests;
    }

    public int getNumberOfPassingTests() {
        return numberOfPassingTests;
    }

    void setNumberOfPassingTests(int numberOfPassingTests) {
        this.numberOfPassingTests = numberOfPassingTests;
    }

    public int getNumberOfFailingTests() {
        return numberOfFailingTests;
    }

    void setNumberOfFailingTests(int numberOfFailingTests) {
        this.numberOfFailingTests = numberOfFailingTests;
    }

    void addNumberOfTests(int tests) {
        numberOfTests += tests;
    }

    void addNumberOfPassingTests(int passes) {
        numberOfPassingTests += passes;
    }

    void addNumberOfFailingTests(int failures) {
        numberOfFailingTests += failures;
    }

    @Override
    public String toString() {
        return String.format("Cypress tests run: %s\n" +
                                     "Cypress tests passing: %s\n" +
                                     "Cypress tests failing: %s",
                             numberOfTests,
                             numberOfPassingTests,
                             numberOfFailingTests);
    }
}