@NHSJobsSearch
Feature: NHS Jobs Search Functionality
  As a jobseeker on the NHS Jobs website
  I want to search for jobs with my preferences
  So that I can get recently posted job results

  Background:
    Given I am on the NHS Jobs search page

  @Smoke @HappyPath
  Scenario: Search for jobs by keyword only
    When I search for jobs with keyword "Nurse"
    And I submit the search
    Then I should see a list of job results
    And each result should be relevant to "Nurse"

  @Smoke @HappyPath
  Scenario: Search for jobs by keyword and location
    When I search for jobs with keyword "Doctor"
    And I enter the location "London"
    And I submit the search
    Then I should see a list of job results
    And the page title should indicate results for "Doctor"

  @HappyPath @SortOrder
  Scenario: Sort search results by newest date posted
    When I search for jobs with keyword "Radiographer"
    And I submit the search
    Then I should see a list of job results
    When I sort the results by "Date Posted (newest)"
    Then the job results should be sorted by "Date Posted" in "descending" order

  @HappyPath @SortOrder
  Scenario: Default search results can be re-sorted by newest date
    When I search for jobs with keyword "Pharmacist"
    And I submit the search
    Then I should see a list of job results
    And I should have the option to sort results
    When I sort the results by "Date Posted (newest)"
    Then the job results should be sorted by "Date Posted" in "descending" order


  @HappyPath @Filters
  Scenario Outline: Search for jobs with different job type keywords
    When I search for jobs with keyword "<keyword>"
    And I submit the search
    Then I should see a list of job results
    And the page title should indicate results for "<keyword>"

    Examples:
      | keyword          |
      | Physiotherapist  |
      | MentalHealth     |
      | Admin            |
      | IT               |

  @HappyPath @Filters
  Scenario: Search with keyword and distance filter
    When I search for jobs with keyword "Midwife"
    And I enter the location "Manchester"
    And I submit the search
    Then I should see a list of job results

  @HappyPath @Pagination
  Scenario: Navigate through multiple pages of search results
    When I search for jobs with keyword "Nurse"
    And I submit the search
    Then I should see a list of job results
    And the total number of results should be displayed
    When I navigate to the next page of results
    Then I should see the next page of job results

  @EdgeCase @Validation
  Scenario: Search with no keyword returns results or prompts user
    When I submit the search without entering any keyword
    Then I should see job results or an appropriate message

  @EdgeCase @Validation
  Scenario: Search with special characters in keyword
    When I search for jobs with keyword "Nurse & Midwife"
    And I submit the search
    Then I should see a list of job results or a no results message

  @EdgeCase @Validation
  Scenario: Search with location only (no keyword)
    When I enter the location "Birmingham"
    And I submit the search
    Then I should see a list of job results or a no results message

  @EdgeCase @NoResults
  Scenario: Search for jobs with no matching results
    When I search for jobs with keyword "ZZZZZNOMATCHEXPECTED99999"
    And I submit the search
    Then I should see a no results message
    And I should see suggestions to broaden my search

  @HappyPath @ResultDetails
  Scenario: Each search result displays required job information
    When I search for jobs with keyword "Consultant"
    And I submit the search
    Then I should see a list of job results
    And each job result should display complete job details


  @HappyPath @ResultDetails
  Scenario: Clicking a job result opens the job detail page
    When I search for jobs with keyword "Nurse"
    And I submit the search
    Then I should see a list of job results
    When I click the first job result
    Then I should be taken to the job detail page
    And the job detail page should display job information

  @HappyPath @SortOrder @DateValidation
  Scenario: Verify date ordering when sorted by newest
    When I search for jobs with keyword "Nurse"
    And I submit the search
    Then I should see a list of job results
    When I sort the results by "Date Posted (newest)"
    Then each job result should display a posted date
    Then the job results should be sorted by "Date Posted" in "descending" order

  @Chrome
  Scenario: Search functionality works correctly in Chrome
    When I search for jobs with keyword "Radiographer"
    And I submit the search
    Then I should see a list of job results
    And the search results page should load completely

  @Firefox
  Scenario: Search functionality works correctly in Firefox
    When I search for jobs with keyword "Radiographer"
    And I submit the search
    Then I should see a list of job results
    And the search results page should load completely
