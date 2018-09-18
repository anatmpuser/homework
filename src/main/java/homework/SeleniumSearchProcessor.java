package homework;

import com.google.gson.Gson;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class purpose is:
 * - search github.com site for repositories by given (hardcoded for now) text
 * - print information about repositories into a file
 *
 * Uses Selenium web driver. The driver must be available in the working directory.
 */
class SeleniumSearchProcessor {
    // Currently, in order to run, must have chromedriver executable in working directory.
    // TODO: create properties file, setup environment variable or add command line parameters
    private final static String WEB_DRIVER_PROPERTY = "webdriver.chrome.driver";
    private final static String WEB_DRIVER_EXECUTABLE = "chromedriver" + (isWindows()?".exe":"");

    // TODO: need to hide the password - use encryption, hold connection properties in configuration file
    /** Connection constants **/
    private final static String INITIAL_URL = "https://github.com/";
    private final static String USER = "anatmpuser";
    private final static String PASSWORD = "anatmppwd1";

    // TODO: consider creation of separate constants class that will hold all search patterns - for easy maintenance
    /** Search constants **/
    private final static String SEARCH_TEXT = "security";
    private final static String SEARCH_FORM_NAME = "q";

    /** Elements location constants **/
    // TODO: improve XPATHS for better stability due to variations in the page format
    private final static String NEXT_PAGE_BTN_XPATH = "//a[@class=\"next_page\"]";
    private final static String REPO_LIST_CLASS = "repo-list";
    private final static String REPO_TITLE_XPATH = ".//h3/a";
    private final static String REPO_DESCRIPTION_XPATH = ".//p";
    private final static String REPO_TAGS_XPATH = ".//a";
    private final static String REPO_TIME_XPATH = ".//relative-time";
    private final static String REPO_LANGUAGE_XPATH = ".//div[span[@class=\"repo-language-color\"]]";
    private final static String REPO_STARS_XPATH = ".//a[@class=\"muted-link\"]";

    /** Output file name preffix **/
    private final static String RESULT_FILE_PREFIX = "SecurityResultGitHub";

    private static WebDriver driver;
    private static PrintWriter resultWriter;

    /**
     * This method does:
     * - invoke selenium
     * - collect all required info from the web site provided
     * - writes result to file
     */
    static void searchRepos() {

        System.out.println("Launching chrome browser");
        System.setProperty(WEB_DRIVER_PROPERTY, WEB_DRIVER_EXECUTABLE);
        driver = new ChromeDriver();

        System.out.println("Signing in to " + INITIAL_URL + " as user: " + USER);
        loginAs(INITIAL_URL + "/login", USER, PASSWORD);

        System.out.println("Searching for: " + SEARCH_TEXT);
        WebElement searchForm = driver.findElement(By.name(SEARCH_FORM_NAME));
        searchForm.sendKeys(SEARCH_TEXT);
        searchForm.submit();

        System.out.println("Getting all search results from first 5 pages");

        // Result will be written to file.
        // Assumption - In case of exception during file operation,
        // it's sufficient that the program will exit with status 1.
        resultWriter = initiateResultWriter();

        for (int page = 1; page <= 5; page++) {
            processCurrentPage();

            WebElement nextPage = driver.findElement(By.xpath(NEXT_PAGE_BTN_XPATH));
            if (nextPage != null && nextPage.isEnabled()) nextPage.click();

            // Wait until the page loaded (to improve - replace sleep by page loaded status check)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                exitGracefully(e);
            }
        }

        if (resultWriter != null) resultWriter.close();
        driver.quit();
    }

    /**
     * Gets a list of repo records, finds on the page data about each repo:
     * Title, description, tags, time, language and stars
     *
     * Creates JSON string for each repo and writes to a file.
     **/
    private static void processCurrentPage() {

        List<WebElement> repoListItems = driver.findElement(By.className(REPO_LIST_CLASS)).findElements(By.xpath("./div"));

        for (WebElement repoListItem : repoListItems) {
            RepoRecord repoRecord = new RepoRecord();

            repoRecord.setTitle(getRepoTitle(repoListItem));
            repoRecord.setDescription(getRepoDescription(repoListItem));
            repoRecord.setTags(getRepoTags(repoListItem));
            repoRecord.setTime(getRepoTime(repoListItem));
            repoRecord.setLanguage(getRepoLanguage(repoListItem));
            repoRecord.setStars(getRepoStars(repoListItem));

            writeRepoRecordToFile(repoRecord);
        }
    }

    // TODO: handle element location failures
    // TODO: consider create separate class for elements retrieval functions
    private static String getRepoStars(WebElement repoListItem) {
        WebElement repoStarsElement = repoListItem.findElement(By.xpath(REPO_STARS_XPATH));
        return (repoStarsElement != null)?repoStarsElement.getText():"";
    }

    private static String getRepoLanguage(WebElement repoListItem) {
        WebElement languageElement = null;
        String language = "";
        try {
            languageElement = repoListItem.findElement(By.xpath(REPO_LANGUAGE_XPATH));
        } catch (Exception e) {
            // language not specified
        }
        if (languageElement != null) {
            language = languageElement.getText();
        }
        return language;
    }

    private static String getRepoTime(WebElement repoListItem) {
        WebElement repoTimeElement = repoListItem.findElement(By.xpath(REPO_TIME_XPATH));
        return repoTimeElement.getAttribute("datetime");
    }

    private static List<String> getRepoTags(WebElement repoListItem) {
        List<WebElement> repoTagsElements = repoListItem.findElements(By.xpath(REPO_TAGS_XPATH));
        List<String> tags = new ArrayList<>();
        for(WebElement repoTagsElement : repoTagsElements) {
            if (repoTagsElement.getAttribute("href").contains("topic")) {
                tags.add(repoTagsElement.getText());
            }
        }
        return tags;
    }

    private static String getRepoTitle(WebElement repoListItem) {
        WebElement repoTitleElement = repoListItem.findElement(By.xpath(REPO_TITLE_XPATH));
        return repoTitleElement.getText();
    }
    private static String getRepoDescription(WebElement repoListItem) {
        WebElement repoDescriptionElement = repoListItem.findElement(By.xpath(REPO_DESCRIPTION_XPATH));
        return repoDescriptionElement.getText();
    }

    // TODO: consider to create separate class for service/helper utils
    private static void loginAs(String siteUrl, String username, String password) {
        driver.get(siteUrl);
        driver.findElement(By.id("login_field")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.name("commit")).submit();
    }

    private static void writeRepoRecordToFile(RepoRecord repoRecord){
        Gson gson = new Gson();
        String repoSearchResults = gson.toJson(repoRecord);
        resultWriter.write(repoSearchResults + "\n");
    }

    private static boolean isWindows()
    {
        String OS = System.getProperty("os.name");
        return OS.startsWith("Windows");
    }

    private static PrintWriter initiateResultWriter() {
        // TODO: consider to remove files from previous run
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
        String dateTimeStr = DATE_TIME_FORMATTER.format(Instant.now());

        String resultFileName = RESULT_FILE_PREFIX + dateTimeStr;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(resultFileName, "UTF-8");
            System.out.println("Result file will be: " + resultFileName);
        } catch (Exception e) {
            exitGracefully(e);
        }
        return writer;
    }

    private static void exitGracefully(Exception e) {
        System.out.println("Exception occurred, exiting: " + e.getLocalizedMessage());
        e.printStackTrace();
        driver.close();
        System.exit(1);
    }
}
