package jobs.pages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.junit.platform.commons.logging.Logger;
//import org.junit.platform.commons.logging.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


/**
 * Page Object for the NHS Jobs Search Page.
 * URL: https://www.jobs.nhs.uk/candidate/search
 *
 * Encapsulates all locators and actions for the search input form.
 * Locators use CSS selectors where possible (more performant than XPath),
 * falling back to name attributes and accessible ARIA attributes for resilience.
 *
 * Locator strategy rationale:
 * - ID selectors: fastest, most reliable — used wherever IDs are stable
 * - Name attributes: stable semantic HTML attributes on form inputs
 * - CSS selectors: performant and readable for class/attribute combinations
 * - XPath: used only when CSS selectors cannot express the required relationship
 * - ARIA labels: resilient to styling changes, accessible-test aligned
 */
public class SearchPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(SearchPage.class);

    // ── Locators ──────────────────────────────────────────────────────────────
    // These are based on the NHS Jobs candidate search page structure.
    // Primary strategy: name/id attributes on form fields (most stable).
    // Fallback: CSS selectors using data attributes or ARIA roles.

    /** Keyword search input field */
    private static final By KEYWORD_INPUT = By.id("keyword");

    /** Location input field */
    private static final By LOCATION_INPUT = By.id("location");

    /** Primary search / submit button */
    private static final By SEARCH_BUTTON = By.cssSelector("button[type='submit'], input[type='submit'], [data-module='nhsuk-button']");

    /** Alternative search button locator by value text */
    private static final By SEARCH_BUTTON_BY_TEXT = By.xpath("//button[contains(text(),'Search') or @type='submit']");

    // ── Constructor ───────────────────────────────────────────────────────────

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Enters a keyword into the search field.
     * Clears any existing value before typing.
     */
    public SearchPage enterKeyword(String keyword) {
        logger.info("Entering keyword: '{}'", keyword);
        typeText(KEYWORD_INPUT, keyword);
        return this;
    }

    /**
     * Enters a location into the location field.
     */
    public SearchPage enterLocation(String location) {
        logger.info("Entering location: '{}'", location);
        typeText(LOCATION_INPUT, location);
        return this;
    }

    /**
     * Clicks the search/submit button.
     * Tries primary locator first; falls back to text-based XPath.
     */
    public SearchResultsPage clickSearchButton() {
        logger.info("Clicking search button");
        try {
            clickElement(SEARCH_BUTTON);
        } catch (Exception e) {
            logger.warn("Primary search button locator failed, attempting fallback XPath");
            clickElement(SEARCH_BUTTON_BY_TEXT);
        }
        return new SearchResultsPage(driver);
    }

    /**
     * Clears the keyword field.
     */
    public SearchPage clearKeyword() {
        waitForVisible(KEYWORD_INPUT).clear();
        return this;
    }

    /**
     * Returns whether the keyword input is displayed.
     */
    public boolean isKeywordInputDisplayed() {
        return isElementVisible(KEYWORD_INPUT);
    }

    /**
     * Returns whether the location input is displayed.
     */
    public boolean isLocationInputDisplayed() {
        return isElementVisible(LOCATION_INPUT);
    }

    /**
     * Returns whether the search button is displayed.
     */
    public boolean isSearchButtonDisplayed() {
        return isElementVisible(SEARCH_BUTTON) || isElementVisible(SEARCH_BUTTON_BY_TEXT);
    }

    /**
     * Convenience method: enter keyword and submit search.
     */
    public SearchResultsPage searchFor(String keyword) {
        enterKeyword(keyword);
        return clickSearchButton();
    }

    /**
     * Convenience method: enter keyword, location and submit search.
     */
    public SearchResultsPage searchFor(String keyword, String location) {
        enterKeyword(keyword);
        enterLocation(location);
        return clickSearchButton();
    }

    /**
     * Submit search without entering any keyword (empty search).
     */
    public SearchResultsPage submitEmptySearch() {
        logger.info("Submitting empty search");
        clearKeyword();
        return clickSearchButton();
    }

    /**
     * Returns the current value of the keyword field.
     */
    public String getKeywordFieldValue() {
        return waitForVisible(KEYWORD_INPUT).getAttribute("value");
    }
}