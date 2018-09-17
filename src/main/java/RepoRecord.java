import java.util.List;

/**
 * Created by anap on 9/16/2018.
 */
public class RepoRecord {
    private String title;
    private String description;
    private List<String> tags;
    private String time;
    private String language;
    private String stars;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public RepoRecord(String title, String description, List<String> tags, String time, String language, String stars) {

        this.title = title;
        this.description = description;
        this.tags = tags;
        this.time = time;
        this.language = language;
        this.stars = stars;
    }

    public RepoRecord() {

        this.title = "";
        this.description = "";
        this.tags = null;
        this.time = "";
        this.language = "";
        this.stars = "";
    }
}
