package io.github.wimdeblauwe.testcontainers.cypress;

import java.util.ArrayList;
import java.util.List;

public class CypressTestResults {
    private int numberOfTests;
    private int numberOfPassingTests;
    private int numberOfFailingTests;
    private List<CypressTestSuite> suites = new ArrayList<>();

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

    public List<CypressTestSuite> getSuites() {
        return suites;
    }

    void addSuites(List<CypressTestSuite> suites) {
        this.suites.addAll(suites);
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