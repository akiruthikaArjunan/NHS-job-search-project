package jobs.pages;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the NHS Jobs individual Job Detail page.
 * Navigated to when a user clicks a job listing from the results page.
 */
public class JobDetailPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private static final By JOB_TITLE_HEADING = By.cssSelector(
            "h1, [data-test='job-title'], .nhsuk-heading-xl");

    private static final By EMPLOYER_NAME = By.cssSelector(
            "[data-test='employer-name'], [class*='employer'], .organisation-name");

    private static final By APPLY_BUTTON = By.cssSelector(
            "a[href*='apply'], button[data-test='apply'], " +
                    "a[data-test='apply-button'], .apply-button");

    private static final By JOB_DESCRIPTION = By.cssSelector(
            "[data-test='job-description'], .job-description, " +
                    "#job-description, article");

    // ── Constructor ───────────────────────────────────────────────────────────

    public JobDetailPage(WebDriver driver) {
        super(driver);
        waitForUrlContaining("job");
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns true if essential job details are visible on the detail page.
     */
    public boolean isJobDetailPageDisplayed() {
        return isElementVisible(JOB_TITLE_HEADING) || isElementVisible(JOB_DESCRIPTION);
    }

    /**
     * Returns the job title displayed on the detail page.
     */
    public String getJobTitle() {
        return getText(JOB_TITLE_HEADING);
    }

    /**
     * Returns true if the employer name is visible on the detail page.
     */
    public boolean isEmployerNameDisplayed() {
        return isElementVisible(EMPLOYER_NAME);
    }

    /**
     * Returns true if the apply button or link is visible.
     */
    public boolean isApplyButtonDisplayed() {
        return isElementVisible(APPLY_BUTTON);
    }

    /**
     * Returns true if the URL corresponds to a job detail page.
     */
    public boolean isOnJobDetailUrl() {
        String url = getCurrentUrl();
        return url.contains("/job/") || url.contains("/vacancy/") || url.contains("jobId");
    }
}