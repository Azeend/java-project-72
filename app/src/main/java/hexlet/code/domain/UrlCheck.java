package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {
    @Id
    private long id;

    @WhenCreated
    private Instant createdAt;

    @Lob
    private String description;

    private String title;

    private String h1;

    private int statusCode;

    @ManyToOne
    private Url url;

    public UrlCheck(String description, String title, String h1, int statusCode, Url url) {
        this.description = description;
        this.title = title;
        this.h1 = h1;
        this.statusCode = statusCode;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Url getUrl() {
        return url;
    }
}
