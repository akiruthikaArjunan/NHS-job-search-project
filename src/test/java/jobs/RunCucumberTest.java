package jobs;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber JUnit Platform test runner.
 *
 * Execution examples:
 *   All tests:          mvn test
 *   Chrome:             mvn test -Dbrowser=chrome
 *   Firefox:            mvn test -Dbrowser=firefox
 *   Headless:           mvn test -Dheadless=true
 *   Tag filter:         mvn test -Dcucumber.tags="@Smoke"
 *   Combined:           mvn test -Dbrowser=firefox -Dcucumber.tags="@HappyPath"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.plugin", value =
        "pretty," +
                "html:target/cucumber-reports/cucumber-report.html," +
                "json:target/cucumber-reports/cucumber-report.json," +
                "junit:target/cucumber-reports/cucumber-report.xml")
@ConfigurationParameter(key = "cucumber.glue", value = "uk.nhs.jobs.steps, uk.nhs.jobs.hooks")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
public class RunCucumberTest {
    // JUnit Platform Suite runner - no body required
}