package jobs.utils;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * DriverFactory manages WebDriver lifecycle.
 *
 * Uses WebDriverManager to automatically download and manage browser drivers,
 * satisfying the requirement: "tests must not run from downloaded or machine-based drivers".
 * WebDriverManager resolves, downloads and caches the required driver binary at runtime.
 */
public class DriverFactory {

    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    private static final int IMPLICIT_WAIT_SECONDS = 10;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 30;

    private DriverFactory() {
        // Utility class - prevent instantiation (not create the object)
    }

    /**
     * Initialises and returns a WebDriver instance for the specified browser.
     * WebDriverManager resolves the correct driver version automatically.
     *
     * @param browser   "chrome" or "firefox"
     * @param headless  run in headless mode when true
     * @return configured WebDriver instance
     */
    public static WebDriver createDriver(String browser, boolean headless) {
        WebDriver driver;

        logger.info("Creating {} WebDriver (headless={})", browser, headless);

        switch (browser.toLowerCase().trim()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = buildFirefoxOptions(headless);
                driver = new FirefoxDriver(firefoxOptions);
            }
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = buildChromeOptions(headless);
                driver = new ChromeDriver(chromeOptions);
            }
            default -> {
                logger.warn("Unknown browser '{}', defaulting to Chrome", browser);
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver(buildChromeOptions(headless));
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().window().maximize();

        driverThreadLocal.set(driver);
        logger.info("WebDriver initialised successfully");
        return driver;
    }

    /**
     * Returns the current thread's WebDriver instance.
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver has not been initialised for this thread. " +
                    "Ensure createDriver() is called before getDriver().");
        }
        return driver;
    }

    /**
     * Quits the WebDriver and clears the ThreadLocal reference.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver quit successfully");
            } catch (Exception e) {
                logger.warn("Exception while quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--window-size=1920,1080",
                "--remote-allow-origins=*"
        );
        // Suppress Chrome logging noise
        options.addArguments("--log-level=3");
        return options;
    }

    private static FirefoxOptions buildFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return options;
    }
}