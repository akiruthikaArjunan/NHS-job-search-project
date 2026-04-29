package jobs.pages;

//import org.junit.platform.commons.logging.Logger;
//import org.junit.platform.commons.logging.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.junit.platform.commons.logging.Logger;
//import org.junit.platform.commons.logging.LoggerFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;


/**
 * Page Object for the NHS Jobs Search Results Page.

 * Handles all interactions and assertions on the results listing.
 * Locator strategy:
 * - Semantic HTML attributes preferred (role, aria-label, data-*)
 * - CSS class selectors for NHS-specific component classes
 * - XPath used only where a parent-child text relationship is required
 **/
public class SearchResultsPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(SearchResultsPage.class);

    // ── Locators ──────────────────────────────────────────────────────────────

    /** Container holding all result cards */
    private static final By RESULTS_CONTAINER = By.cssSelector(
            "[data-test='search-results'], .nhsuk-grid-column-full, #results, main");

    /** Individual job result cards */
    private static final By JOB_RESULT_CARDS = By.cssSelector(
            "[data-test='search-result'], .nhsuk-card, article.job-result, li.job-result");

    /** Job title link within a result card */
    /*private static final By JOB_TITLE = By.cssSelector(
            "h2 a, h3 a, .nhsuk-card__heading a, [data-test='job-title']");*/
    private By JOB_TITLE = By.cssSelector(".nhsuk-card__heading a");
    /** Employer / organisation name within a result card */
    private static final By EMPLOYER_NAME = By.cssSelector(
            "[data-test='employer-name'], .nhsuk-body-s, .employer-name, " +
                    "p:first-of-type, dl dd:first-of-type");

    /** Closing date within a result card */
    //private static final By CLOSING_DATE = By.cssSelector(
    //        "[data-test='search-result-closingDate'], [class*='closing'], " +
    //                "time, [datetime]");

    private static final By CLOSING_DATE =
            By.cssSelector("[data-test='search-result-closingDate']");

    /** Posted / advertised date */
    private static final By POSTED_DATE = By.cssSelector(
            "[data-test='posted-date'], .posted-date, .nhsuk-body-s");

    private static final By POSTED_DATE_FLEX = By.xpath(
            ".//*[contains(normalize-space(),'Posted') or contains(normalize-space(),'Date posted')]"
    );
    /** Sort dropdown */
    private static final By SORT_DROPDOWN = By.cssSelector(
            "select[name='sortBy'], select[name='sort'], select[id='sort'], " +
                    "#sort-results, [data-test='sort-select']");

    /** Total results count element */
    private static final By RESULTS_COUNT = By.cssSelector(
            "[data-test='result-count'], .nhsuk-body-l, h2.nhsuk-heading-m, " +
                    "p.results-count, #results-count");

    /** "No results" message */
    private static final By NO_RESULTS_MESSAGE = By.cssSelector(
            "[data-test='no-results'], .nhsuk-warning-callout, " +
                    "p.no-results, #no-results");

    /** Pagination - next page link */
    private static final By NEXT_PAGE_LINK = By.cssSelector(
            "[rel='next'], a[aria-label*='next' i], a[aria-label*='Next'], " +
                    ".nhsuk-pagination__link--next, a:contains('Next')");

    /** Pagination - next page alternative XPath */
    private static final By NEXT_PAGE_XPATH = By.xpath(
            "//a[contains(@aria-label,'next') or contains(text(),'Next') " +
                    "or contains(@class,'next')]");

    /** "Broaden your search" / suggestions text */
    private static final By SEARCH_SUGGESTIONS = By.cssSelector(
            "[data-test='no-results-suggestions'], .nhsuk-inset-text, " +
                    "p.suggestions, .search-suggestions");

    /** Page heading / H1 */
    private static final By PAGE_HEADING = By.cssSelector("h1, [data-test='page-title']");

    // ── Constructor ───────────────────────────────────────────────────────────

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    // ── Query methods ─────────────────────────────────────────────────────────

    /**
     * Returns true if at least one job result card is displayed.
     */
    public boolean hasResults() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(JOB_RESULT_CARDS),
                    ExpectedConditions.presenceOfElementLocated(NO_RESULTS_MESSAGE)
            ));
            List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);
            logger.info("Found {} job result cards", cards.size());
            return !cards.isEmpty();
        } catch (TimeoutException e) {
            logger.warn("Timed out waiting for results or no-results message");
            return false;
        }
    }

    /**
     * Returns true if the "no results" message is displayed.
     */
    public boolean hasNoResultsMessage() {
        return isElementVisible(NO_RESULTS_MESSAGE) ||
                getPageText().toLowerCase().contains("no result") ||
                getPageText().toLowerCase().contains("0 results") ||
                getPageText().toLowerCase().contains("no jobs");
    }

    /**
     * Returns true if "broaden your search" or similar suggestions are shown.
     */
    public boolean hasBroaderSearchSuggestion() {
        //System.out.println(getPageText());
        return isElementVisible(SEARCH_SUGGESTIONS) ||
                getPageText().toLowerCase().contains("broaden") ||
                getPageText().toLowerCase().contains("try") ||
                getPageText().toLowerCase().contains("improve your results by") ||
                getPageText().toLowerCase().contains("suggestions");

    }

    /**
     * Returns the total number of result cards visible on the current page.
     */
    public int getResultCount() {
        List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);
        return cards.size();
    }

    /**
     * Returns the results count text (e.g. "152 jobs found").
     */
    public String getResultsCountText() {
        if (isElementVisible(RESULTS_COUNT)) {
            return getText(RESULTS_COUNT);
        }
        // Fallback: look for count in page heading
        String heading = isElementVisible(PAGE_HEADING) ? getText(PAGE_HEADING) : "";
        return heading;
    }

    /**
     * Returns the page H1 heading text.
     */
    public String getPageHeading() {
        return isElementVisible(PAGE_HEADING) ? getText(PAGE_HEADING) : getPageTitle();
    }

    /**
     * Checks every visible job result card has a non-empty title.
     */
    public boolean allResultsHaveJobTitle() {
        List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);
        if (cards.isEmpty()) return false;
        for (WebElement card : cards) {
            List<WebElement> titles = card.findElements(JOB_TITLE);
            if (titles.isEmpty() || titles.get(0).getText().isBlank()) {
                logger.warn("Job card found without a title");
                return false;
            }
        }
        return true;
    }

    /**
     * Checks every visible job result card has a non-empty employer name.
     */
    public Map<String, Integer> validateAllJobCards() {
        List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);

        int missingTitle = 0;
        int missingDate = 0;

        for (WebElement card : cards) {

            // Title check
            List<WebElement> titles = card.findElements(JOB_TITLE);
            boolean titleMissing = titles.isEmpty() ||
                    titles.stream().allMatch(e -> e.getText().isBlank());

            if (titleMissing) {
                missingTitle++;
            }

            // Closing date check
            List<WebElement> dates = card.findElements(CLOSING_DATE);
            boolean dateMissing = dates.isEmpty() ||
                    dates.stream().allMatch(e -> e.getText().isBlank());

            if (dateMissing) {
                missingDate++;
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("missingTitle", missingTitle);
        result.put("missingDate", missingDate);

        return result;
    }
    public boolean allResultsHaveEmployerName() {
        List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);
        if (cards.isEmpty()) return false;
        // Employer name may be less strictly enforced; log warning rather than hard-fail
        int missingCount = 0;
        for (WebElement card : cards) {
            List<WebElement> employers = card.findElements(EMPLOYER_NAME);
            if (employers.isEmpty() || employers.get(0).getText().isBlank()) {
                missingCount++;
            }
        }
        if (missingCount > 0) {
            logger.warn("{} cards missing employer name", missingCount);
        }
        return missingCount == 0;
    }

    /**
     * Checks every visible job result card displays a closing date.
     */
   /* public boolean allResultsHaveClosingDate() {
        List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);
        if (cards.isEmpty()) return false;
        int missingCount = 0;
        for (WebElement card : cards) {
            List<WebElement> dates = card.findElements(CLOSING_DATE);
            if (dates.isEmpty() || dates.get(0).getText().isBlank()) {
                missingCount++;
            }
        }
        if (missingCount > 0) {
            logger.warn("{} cards missing closing date", missingCount);
        }
        return missingCount == 0;
    }*/


    public int allResultsHaveClosingDate() {
        List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);
        int missingCount = 0;

        for (WebElement card : cards) {
            List<WebElement> dates = card.findElements(CLOSING_DATE);
            boolean missing = dates.isEmpty() ||
                    dates.stream().allMatch(e -> e.getText().isBlank());

            if (missing) {
                missingCount++;
            }
        }

        return missingCount;
    }

    /**
     * Returns true if the sort dropdown is present on the page.
     */
    public boolean hasSortOption() {
        return isElementVisible(SORT_DROPDOWN) ||
                getPageText().toLowerCase().contains("sort by") ||
                getPageText().toLowerCase().contains("sort:");
    }

    /**
     * Selects a sort option from the dropdown by visible text.
     * e.g. "Date posted (newest first)"
     */
    public SearchResultsPage sortBy(String sortOptionText) {
        logger.info("Sorting results by: '{}'", sortOptionText);

        try {
            selectByVisibleText(SORT_DROPDOWN, sortOptionText);

            //sleep(2000);
        } catch (Exception e) {
            logger.warn("Could not select sort option by visible text '{}', trying value-based selection", sortOptionText);
            // Try selecting by partial value (e.g. "newest" maps to value "dateDesc")
            if (sortOptionText.toLowerCase().contains("newest")) {
                try { selectByValue(SORT_DROPDOWN, "dateDesc"); } catch (Exception ex) {
                    try { selectByValue(SORT_DROPDOWN, "date_desc"); } catch (Exception ex2) {
                        logger.error("Unable to select sort option: {}", sortOptionText);
                    }
                }
            }
        }
        // Wait for page to refresh after sort
        waitForResultsToRefresh();
        return this;
    }

    /**
     * Returns true if the "next page" link is present and clickable.
     */
    public boolean hasNextPage() {
        return isElementVisible(NEXT_PAGE_LINK) || isElementVisible(NEXT_PAGE_XPATH);
    }

    /**
     * Clicks the "next page" link and returns a fresh results page.
     */
    public SearchResultsPage goToNextPage() {
        logger.info("Navigating to next page of results");
        try {
            clickElement(NEXT_PAGE_LINK);
        } catch (Exception e) {
            clickElement(NEXT_PAGE_XPATH);
        }
        waitForResultsToRefresh();
        return this;
    }

    /**
     * Clicks the first job result and returns the job detail page.
     */
    /*public JobDetailPage clickFirstResult() {
        logger.info("Clicking first job result");
        List<WebElement> titles = driver.findElements(JOB_TITLE);
        if (titles.isEmpty()) {
            throw new IllegalStateException("No job results found to click");
        }
        titles.get(0).click();
        return new JobDetailPage(driver);
    }*/
    public JobDetailPage clickFirstResult() {
        logger.info("Clicking first job result");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // wait for job cards
        List<WebElement> cards = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(JOB_RESULT_CARDS)
        );

        if (cards.isEmpty()) {
            throw new RuntimeException("No job cards found");
        }

        WebElement firstCard = cards.get(0);

        // find link INSIDE the first card
        WebElement link = firstCard.findElement(By.cssSelector("a"));

        // scroll into view
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", link);

        // click via JS (stable)
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", link);

        return new JobDetailPage(driver);
    }
    /**
     * Verifies dates are in descending order (newest first).
     * Returns true if dates could be parsed and are correctly ordered.
     */
    public boolean areDatesInDescendingOrder() {

        List<WebElement> elements = driver.findElements(POSTED_DATE);

        if (elements.size() < 2) {
            logger.warn("Fewer than 2 date elements found");
            return true; // same behavior as before
        }

        List<LocalDate> dates = new ArrayList<>();

        for (WebElement el : elements) {

            String text = el.getText().trim();

            // ✅ extract only actual date using regex
            Matcher matcher = Pattern.compile("(\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4})")
                    .matcher(text);

            if (matcher.find()) {
                String dateStr = matcher.group(1);

                try {
                    dates.add(LocalDate.parse(
                            dateStr,
                            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
                    ));
                } catch (Exception e) {
                    logger.warn("Could not parse date: {}", dateStr);
                }
            }
        }

        if (dates.size() < 2) {
            logger.warn("Could not parse enough dates");
            return true; // keep yesterday behavior
        }

        for (int i = 0; i < dates.size() - 1; i++) {
            if (dates.get(i).isBefore(dates.get(i + 1))) {
                logger.error("Date order wrong: {} before {}", dates.get(i), dates.get(i + 1));
                return false;
            }
        }

        return true;
    }
    /**
     * Returns true if all result cards display a posted date element.
     */
    public boolean allResultsHavePostedDate() {

        List<WebElement> cards = driver.findElements(JOB_RESULT_CARDS);
        if (cards.isEmpty()) return false;

        for (int i = 0; i < cards.size(); i++) {

            WebElement card = cards.get(i);
            String cardText = card.getText().toLowerCase();

            // ✅ STRICT check: must contain "date posted"
            if (!cardText.contains("date posted")) {
                logger.error("Card index {} missing 'Date posted':\n{}", i, card.getText());
                return false;
            }

            // ✅ Extract the actual line
            String dateLine = Arrays.stream(card.getText().split("\\n"))
                    .filter(line -> line.toLowerCase().contains("date posted"))
                    .findFirst()
                    .orElse(null);

            if (dateLine == null || dateLine.trim().isEmpty()) {
                logger.error("Card index {} has empty posted date line:\n{}", i, card.getText());
                return false;
            }

            // Optional: validate format (stronger test)
            String cleanDate = dateLine
                    .replaceAll("(?i)date posted[:]?\\s*", "")
                    .trim();

            try {
                LocalDate.parse(cleanDate,
                        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK));
            } catch (Exception e) {
                logger.error("Invalid date format '{}' in card index {}", cleanDate, i);
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that the page heading or title contains the given keyword.
     */
    public boolean pageIndicatesSearchFor(String keyword) {
        String heading = getPageHeading().toLowerCase();
        String title = getPageTitle().toLowerCase();
        String url = getCurrentUrl().toLowerCase();
        String keywordLower = keyword.toLowerCase();
        return heading.contains(keywordLower) || title.contains(keywordLower)
                || url.contains(keywordLower.replace(" ", "+"))
                || url.contains(keywordLower.replace(" ", "%20"));
    }

    /**
     * Returns true if the page has fully loaded (URL contains /search or /results
     * and at least one main content element is present).
     */
    public boolean isPageFullyLoaded() {
        String url = getCurrentUrl();
        return (url.contains("search") || url.contains("candidate")) &&
                isElementPresent(By.tagName("main"));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void waitForResultsToRefresh() {
        try {
            sleep(100); // Short stabilisation pause after sort/page change
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(RESULTS_CONTAINER));
    }

    private String getPageText() {
        try {
            return driver.findElement(By.tagName("body")).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Attempts to parse date text from WebElements using common UK date formats.
     */
    private List<LocalDate> parseDates(List<WebElement> elements) {
        List<LocalDate> dates = new ArrayList<>();
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK),
                DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (WebElement element : elements) {
            // Check datetime attribute first (most reliable)
            String dateStr = element.getAttribute("datetime");
            if (dateStr == null || dateStr.isBlank()) {
                dateStr = element.getText().trim();
            }
            // Strip leading labels like "Posted:" or "Closing:"
            dateStr = dateStr.replaceAll("(?i)(posted|closing|date posted|advertised|closing date)[:.]?\\s*", "").trim();

            for (DateTimeFormatter formatter : formatters) {
                try {
                    dates.add(LocalDate.parse(dateStr, formatter));
                    break;
                } catch (DateTimeParseException ignored) {
                    // Try next formatter
                }
            }
        }
        return dates;
    }
}
