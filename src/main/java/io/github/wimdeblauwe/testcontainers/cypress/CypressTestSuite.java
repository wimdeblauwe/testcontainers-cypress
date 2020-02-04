package io.github.wimdeblauwe.testcontainers.cypress;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class CypressTestSuite {
    private List<CypressTest> tests = new ArrayList<>();
    private String title;

    public CypressTestSuite(String title) {

        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<CypressTest> getTests() {
        return tests;
    }

    void add(CypressTest cypressTest) {
        tests.add(cypressTest);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CypressTestSuite.class.getSimpleName() + "[", "]")
                .add("title='" + title + "'")
                .add("tests=" + tests.size())
                .toString();
    }
}
