import com.google.gson.Gson;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class purpose is:
 * - search github.com site for repositories by given (hardcoded for now) text
 * - print information about repositories into a file
 *
 * Uses Selenium web driver. The driver must be available in the working directory.
 */
public class seleniumSearchProcessor {
    // Currently, in order to run, must have chromedriver executable in working directory.
    // TODO: create properties file, setup environment variable or add command line parameters
    final static String WEB_DRIVER_PROP = "webdriver.chrome.driver";
    final static String WEB_DRVR_EXECUTABLE= "chromedriver" + (isWindows()?".exe":"");

    /** Connection constants **/
    final static String INITIAL_URL = "https://github.com/";
    final static String USER = "anatmpuser";
    final static String PASSWORD = "anatmppwd1";

    /** Search constants **/
    final static String SEARCH_TEXT = "security";
    final static String SEARCH_FORM_NAME = "q";

    final static String NEXT_PAGE_BTN_XPATH = "//a[@class=\"next_page\"]";
    final static String REPO_LIST_CLASS = "repo-list";
    final static String REPO_TITLE_XPATH = ".//h3/a";
    final static String REPO_DESCRIPTION_XPATH = ".//p";
    final static String REPO_TAGS_XPATH = ".//a";
    final static String REPO_TIME_XPATH = ".//relative-time";
    final static String REPO_LANGUAGE_XPATH = ".//div[span[@class=\"repo-language-color\"]]";
    final static String REPO_STARS_XPATH = ".//a[@class=\"muted-link\"]";

    /** Output file name preffix **/
    final static String RESULT_FILE_PREFIX = "SecurityResultGitHub";

    private static WebDriver driver;
    private static PrintWriter resultWriter;

    public static void main(String []args) {

        System.out.println("Launching chrome browser");
        System.setProperty(WEB_DRIVER_PROP, WEB_DRVR_EXECUTABLE);
        driver = new ChromeDriver();

        System.out.println("Signing in to github with USER: " + USER);
        loginAs(USER, PASSWORD);

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

        for(int i = 0; i < repoListItems.size(); ++i) {
            RepoRecord repoRecord = new RepoRecord();
            WebElement repoListItem = repoListItems.get(i);

            // Title, description, tags, time, language and stars
            repoRecord.setTitle(getRepoTitle(repoListItem));
            repoRecord.setDescription(getRepoDescription(repoListItem));
            repoRecord.setTags(getRepoTags(repoListItem));
            repoRecord.setTime(getRepoTime(repoListItem));
            repoRecord.setTime(getRepoLanguage(repoListItem));
            repoRecord.setStars(getRepoStars(repoListItem));

            writeRepoRecordToFile(repoRecord);
        }
    }

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
        String datetime = repoTimeElement.getAttribute("datetime");
        return datetime;
    }

    private static List<String> getRepoTags(WebElement repoListItem) {
        List<WebElement> repoTagsElements = repoListItem.findElements(By.xpath(REPO_TAGS_XPATH));
        List<String> tags = new ArrayList<>();
        for(int j = 0; j < repoTagsElements.size(); ++j) {
            if (repoTagsElements.get(j).getAttribute("href").contains("topic")) {
                tags.add(repoTagsElements.get(j).getText());
            }
        }
        return tags;
    }

    private static String getRepoTitle(WebElement repoListItem) {
        WebElement repoTitleElement = repoListItem.findElement(By.xpath(REPO_TITLE_XPATH));
        String title = repoTitleElement.getText();

        return title;
    }
    private static String getRepoDescription(WebElement repoListItem) {
        WebElement repoDescriptionElement = repoListItem.findElement(By.xpath(REPO_DESCRIPTION_XPATH));
        String description = repoDescriptionElement.getText();

        return description;
    }

    public static void loginAs(String username, String password) {
        driver.get(INITIAL_URL + "/login");
        driver.findElement(By.id("login_field")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.name("commit")).submit();
    }

    private static void writeRepoRecordToFile(RepoRecord repoRecord){
        Gson gson = new Gson();
        String repoSearchResults = gson.toJson(repoRecord);
        resultWriter.write(repoSearchResults + "\n");
    }

    public static boolean isWindows()
    {
        String OS = System.getProperty("os.name");
        return OS.startsWith("Windows");
    }

    private static PrintWriter initiateResultWriter() {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());
        String dateTimeStr = DATE_TIME_FORMATTER.format(Instant.now());

        String resultFileName = RESULT_FILE_PREFIX + dateTimeStr;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(resultFileName, "UTF-8");
            System.out.println("Result file will be: " + resultFileName);
        } catch (FileNotFoundException e) {
            exitGracefully(e);
        } catch (UnsupportedEncodingException e) {
            exitGracefully(e);
        }
        return writer;
    }

    private static void exitGracefully(Exception e) {
        e.printStackTrace();
        driver.close();
        System.exit(1);
    }
}
