package homework;
import java.util.List;

class RepoRecord {
    private String title;
    private String description;
    private List<String> tags;
    private String time;
    private String language;
    private String stars;

    void setTitle(String title) {
        this.title = title;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setTags(List<String> tags) {
        this.tags = tags;
    }

    void setTime(String time) {
        this.time = time;
    }

    void setLanguage(String language) {
        this.language = language;
    }

    void setStars(String stars) {
        this.stars = stars;
    }

    RepoRecord() {

        this.title = "";
        this.description = "";
        this.tags = null;
        this.time = "";
        this.language = "";
        this.stars = "";
    }
}
