package whats.app.meme.model;

import com.google.gson.annotations.SerializedName;

public class Meme {
    @SerializedName("url")
    private String url;

    @SerializedName("title")
    private String title;

    @SerializedName("subreddit")
    private String subreddit;

    @SerializedName("author")
    private String author;

    @SerializedName("postLink")
    private String postLink;

    public Meme(String url, String title, String subreddit, String author, String postLink) {
        this.url = url;
        this.title = title;
        this.subreddit = subreddit;
        this.author = author;
        this.postLink = postLink;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getAuthor() {
        return author;
    }

    public String getPostLink() {
        return postLink;
    }
}
