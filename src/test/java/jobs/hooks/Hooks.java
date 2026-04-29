package jobs.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.junit.platform.commons.logging.Logger;
//import org.junit.platform.commons.logging.LoggerFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import jobs.config.ConfigReader;
import jobs.utils.DriverFactory;


/**
 * Cucumber lifecycle hooks.
 *
 * @Before  — runs before each scenario: initialises the WebDriver.
 * @After   — runs after each scenario: takes a screenshot on failure, then quits the driver.
 */
public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Before(order = 1)
    public void setUp(Scenario scenario) {

        String browser = ConfigReader.getBrowser(); // default
        boolean headless = ConfigReader.isHeadless();

        // ✅ THIS is the missing piece
        if (scenario.getSourceTagNames().contains("@Firefox")) {
            browser = "firefox";
        }

        logger.info("Starting Scenario: '{}' on {}", scenario.getName(), browser);

        DriverFactory.createDriver(browser, headless);
    }


    @After(order = 1)
    public void tearDown(Scenario scenario) {
        WebDriver driver;
        try {
            driver = DriverFactory.getDriver();
        } catch (IllegalStateException e) {
            logger.warn("No active WebDriver to tear down");
            return;
        }

        if (scenario.isFailed()) {
            logger.warn("Scenario FAILED: '{}' — capturing screenshot", scenario.getName());
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
            } catch (Exception e) {
                logger.error("Could not capture screenshot: {}", e.getMessage());
            }
        }

        logger.info("Finished Scenario: '{}' — Status: {}",
                scenario.getName(), scenario.getStatus());
        DriverFactory.quitDriver();
    }
}