package jobs.config;



//Reads test configuration from system properties


public class ConfigReader {

    public static final String BASE_URL = "https://www.jobs.nhs.uk/candidate/search";

    private ConfigReader() {}

    public static String getBrowser() {
        return System.getProperty("browser", "chrome");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    public static String getBaseUrl() {
        return System.getProperty("baseUrl", BASE_URL);
    }
}