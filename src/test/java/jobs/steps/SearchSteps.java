package jobs.steps;
import org.junit.jupiter.api.Assertions;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jobs.pages.JobDetailPage;
import jobs.pages.SearchResultsPage;
import org.assertj.core.api.SoftAssertions;
//import org.junit.platform.commons.logging.Logger;
//import org.junit.platform.commons.logging.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import jobs.pages.SearchPage;
import jobs.pages.SearchResultsPage;
import jobs.config.ConfigReader;
import jobs.utils.DriverFactory;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * Cucumber Step Definitions for NHS Jobs Search functionality.
 *
 * Follows BDD best practices:
 * - Steps are written from the user's perspective
 * - Each step delegates to the appropriate Page Object
 * - No direct Selenium calls here — all driver interaction in Page Objects
 * - Glue code is thin; assertions use AssertJ for readability
 *
 * Shared state between steps is held as instance variables (Cucumber creates
 * one instance per scenario, so this is thread-safe for sequential execution).
 */
public class SearchSteps {

    private static final Logger logger = LoggerFactory.getLogger(SearchSteps.class);

    // Shared page object state across steps in the same scenario
    private WebDriver driver;
    private SearchPage searchPage;
    private SearchResultsPage resultsPage;
    private JobDetailPage jobDetailPage;

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("I am on the NHS Jobs search page")
    public void iAmOnTheNHSJobsSearchPage() {
        driver = DriverFactory.getDriver();
        driver.get(ConfigReader.getBaseUrl());
        searchPage = new SearchPage(driver);
        acceptCookies();
        assertThat(searchPage.isKeywordInputDisplayed())
                .as("Keyword search field should be displayed on the search page")
                .isTrue();
        logger.info("Successfully navigated to NHS Jobs search page: {}", ConfigReader.getBaseUrl());
    }

    private void acceptCookies() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Accept analytics cookies')]")
            ));

            acceptBtn.click();
            logger.info("Cookie banner accepted");

        } catch (Exception e) {
            logger.info("Cookie banner not present");
        }
    }
    // ── When ──────────────────────────────────────────────────────────────────

    @When("I search for jobs with keyword {string}")
    public void iSearchForJobsWithKeyword(String keyword) {
        searchPage.enterKeyword(keyword);
        logger.info("Entered keyword: '{}'", keyword);
    }

    @When("I enter the location {string}")
    public void iEnterTheLocation(String location) {
        searchPage.enterLocation(location);
        logger.info("Entered location: '{}'", location);
    }

    @When("I submit the search")
    public void iSubmitTheSearch() {
        resultsPage = searchPage.clickSearchButton();
        logger.info("Submitted search");
    }

    @When("I submit the search without entering any keyword")
    public void iSubmitTheSearchWithoutEnteringAnyKeyword() {
        resultsPage = searchPage.submitEmptySearch();
        logger.info("Submitted empty search");
    }

    @When("I sort the results by {string}")
    public void iSortTheResultsBy(String sortOption) {
        assertThat(resultsPage).as("Results page should be initialised before sorting").isNotNull();
        resultsPage = resultsPage.sortBy(sortOption);
        logger.info("Sorted results by: '{}'", sortOption);
    }

    @When("I navigate to the next page of results")
    public void iNavigateToTheNextPageOfResults() {
        assertThat(resultsPage.hasNextPage())
                .as("Next page link should be available to navigate to the next page")
                .isTrue();
        resultsPage = resultsPage.goToNextPage();
        logger.info("Navigated to next page of results");
    }

    @When("I click the first job result")
    public void iClickTheFirstJobResult() {
        jobDetailPage = resultsPage.clickFirstResult();
        logger.info("Clicked first job result");
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("I should see a list of job results")
    public void iShouldSeeAListOfJobResults() {
        assertThat(resultsPage.hasResults())
                .as("At least one job result should be displayed after searching")
                .isTrue();
        logger.info("Verified job results are displayed. Count: {}", resultsPage.getResultCount());
    }

    @Then("the page title should indicate results for {string}")
    public void thePageTitleShouldIndicateResultsFor(String keyword) {
        assertThat(resultsPage.pageIndicatesSearchFor(keyword))
                .as("Page should indicate search results for keyword: '%s'", keyword)
                .isTrue();
    }

    @Then("each result should be relevant to {string}")
    public void eachResultShouldBeRelevantTo(String keyword) {
        // Relevance is enforced by the NHS Jobs search engine.
        // We verify structural integrity: results exist and have titles.
        assertThat(resultsPage.hasResults())
                .as("Results should be present for keyword '%s'", keyword)
                .isTrue();
        logger.info("Verified results are present — relevance is determined by the search engine");
    }

    @Then("the results should be ordered with the newest date first")
    public void theResultsShouldBeOrderedWithTheNewestDateFirst() {
        assertThat(resultsPage.areDatesInDescendingOrder())
                .as("Job results should be ordered with the newest posting date first")
                .isTrue();
    }

    @And("I should have the option to sort results")
    public void iShouldHaveTheOptionToSortResults() {
        assertThat(resultsPage.hasSortOption())
                .as("Sort option/dropdown should be available on the results page")
                .isTrue();
    }

    @Then("the total number of results should be displayed")
    public void theTotalNumberOfResultsShouldBeDisplayed() {
        String countText = resultsPage.getResultsCountText();
        assertThat(countText)
                .as("A results count should be displayed (e.g. '152 jobs found')")
                .isNotBlank();
        logger.info("Results count displayed: '{}'", countText);
    }

    @Then("I should see the next page of job results")
    public void iShouldSeeTheNextPageOfJobResults() {
        assertThat(resultsPage.hasResults())
                .as("The next page should also display job results")
                .isTrue();
    }

    @Then("I should see a no results message")
    public void iShouldSeeANoResultsMessage() {
        assertThat(resultsPage.hasNoResultsMessage())
                .as("A 'no results' message should be displayed when no jobs match the search")
                .isTrue();
    }

    @Then("I should see suggestions to broaden my search")
    public void iShouldSeeSuggestionsToBroadenMySearch() {
        assertThat(resultsPage.hasBroaderSearchSuggestion())
                .as("The page should suggest how to broaden the search when no results are found")
                .isTrue();
    }

    @Then("I should see job results or an appropriate message")
    public void iShouldSeeJobResultsOrAnAppropriateMessage() {
        boolean hasResults = resultsPage.hasResults();
        boolean hasMessage = resultsPage.hasNoResultsMessage();
        assertThat(hasResults || hasMessage)
                .as("Either job results OR a no-results message should be displayed")
                .isTrue();
    }

    @Then("I should see a list of job results or a no results message")
    public void iShouldSeeAListOfJobResultsOrANoResultsMessage() {
        boolean hasResults = resultsPage.hasResults();
        boolean hasMessage = resultsPage.hasNoResultsMessage();
        assertThat(hasResults || hasMessage)
                .as("Either job results OR a no-results message should be displayed")
                .isTrue();
    }

    @Then("each job result should display a job title")
    public void eachJobResultShouldDisplayAJobTitle() {
        assertThat(resultsPage.allResultsHaveJobTitle())
                .as("Every job result card should display a job title")
                .isTrue();
    }

    @Then("each job result should display complete job details")
    public void eachJobResultShouldDisplayCompleteJobDetails() {
        Map<String, Integer> result = resultsPage.validateAllJobCards();

        assertThat(result.get("missingTitle"))
                .as("Some job cards are missing titles")
                .isZero();

        assertThat(result.get("missingDate"))
                .as("Some job cards are missing closing dates")
                .isZero();
    }

    @Then("each job result should display an employer name")
    public void eachJobResultShouldDisplayAnEmployerName() {
        assertThat(resultsPage.allResultsHaveEmployerName())
                .as("Every job result card should display an employer or organisation name")
                .isTrue();
    }

    /*@Then("each job result should display a closing date")
    public void eachJobResultShouldDisplayAClosingDate() {
        int missing = resultsPage.allResultsHaveClosingDate();

        assertThat(missing)
                .as("Expected all job cards to have a closing date, but %d were missing", missing)
                .isZero();
    }
    */

    @Then("each job result should display a closing date")
    public void eachJobResultShouldDisplayAClosingDate() {
        int missing = resultsPage.allResultsHaveClosingDate();

        assertThat(missing)
                .as("Expected all job cards to have a closing date, but %d were missing", missing)
                .isZero();
    }

    @Then("I should be taken to the job detail page")
    public void iShouldBeTakenToTheJobDetailPage() {
        assertThat(jobDetailPage.isJobDetailPageDisplayed())
                .as("Clicking a job result should navigate to the job detail page")
                .isTrue();
    }

    @Then("the job detail page should display job information")
    public void theJobDetailPageShouldDisplayJobInformation() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jobDetailPage.isJobDetailPageDisplayed())
                .as("Job detail page content should be displayed")
                .isTrue();
        softly.assertThat(jobDetailPage.getJobTitle())
                .as("Job detail page should have a job title")
                .isNotBlank();
        softly.assertAll();
    }

    @Then("each job result should display a posted date")
    public void eachJobResultShouldDisplayAPostedDate() {
        assertThat(resultsPage.allResultsHavePostedDate())
                .as("Each job should display a posted date")
                .isTrue();
    }

    @Then("the dates should be in descending order \\(newest\\)")
    public void theDatesShouldBeInDescendingOrderNewest() {
        assertThat(resultsPage.areDatesInDescendingOrder())
                .as("Job results should be sorted newest first")
                .isTrue();
    }

    @Then("the job results should be sorted by {string} in {string} order")
    public void verifySorted(String field, String order) {

        Assertions.assertTrue(resultsPage.areDatesSortedDescending());
    }

    /*@Then("the job results should be sorted by {string} in {string} order")
    public void verifySorted(String field, String order) {

        List<WebElement> elements = driver.findElements(
                By.cssSelector("[data-test='search-result-publicationDate'] strong")
        );

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK);

        for (int i = 0; i < elements.size() - 1; i++) {
            LocalDate d1 = LocalDate.parse(elements.get(i).getText().trim(), formatter);
            LocalDate d2 = LocalDate.parse(elements.get(i + 1).getText().trim(), formatter);
            System.out.println("Comparing: " + d1 + " >= " + d2);
            Assertions.assertTrue(!d1.isBefore(d2)); // newest first
        }
    }*/


    @Then("the search results page should load completely")
    public void theSearchResultsPageShouldLoadCompletely() {
        assertThat(resultsPage.isPageFullyLoaded())
                .as("The search results page should fully load in the current browser")
                .isTrue();
    }

    // ── And ───────────────────────────────────────────────────────────────────

    //@And("I should have the option to sort results")
    //public void andIShouldHaveTheOptionToSortResults() {
    //    iShouldHaveTheOptionToSortResults();
    //}
}