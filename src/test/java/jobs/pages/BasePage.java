package jobs.pages;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;



public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    private static final int DEFAULT_WAIT_SECONDS = 15;


    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS));
    }


    protected WebElement waitForVisible(By locator) {
        logger.debug("Waiting for element to be visible: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void clickElement(By locator) {
        logger.debug("Clicking element: {}", locator);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollIntoView(element);
        element.click();
    }

    protected void typeText(By locator, String text) {
        logger.debug("Typing '{}' into element: {}", text, locator);
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }


    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }


    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    /*protected boolean isElementVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }*/

    protected boolean isElementVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }


    protected List<WebElement> findAllElements(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return driver.findElements(locator);
    }


    protected void selectByVisibleText(By locator, String text) {
        logger.debug("Selecting '{}' from dropdown: {}", text, locator);
        WebElement selectElement = waitForVisible(locator);
        new Select(selectElement).selectByVisibleText(text);
    }


    protected void selectByValue(By locator, String value) {
        logger.debug("Selecting value '{}' from dropdown: {}", value, locator);
        WebElement selectElement = waitForVisible(locator);
        new Select(selectElement).selectByValue(value);
    }


    protected String getSelectedDropdownText(By locator) {
        WebElement selectElement = waitForVisible(locator);
        return new Select(selectElement).getFirstSelectedOption().getText().trim();
    }


    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }


    protected void waitForTitleContaining(String text) {
        wait.until(ExpectedConditions.titleContains(text));
    }


    protected String getPageTitle() {
        return driver.getTitle();
    }


    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }


    protected void waitForUrlContaining(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }
}