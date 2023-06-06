package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.javalin.Javalin;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();

        /*Url url = new Url("https://twitch.com");
        url.save();

        Url url2 = new Url("https://youtube.com");
        url2.save();*/

        mockWebServer = new MockWebServer();
        MockResponse mockResponse = new MockResponse()
                .setBody(Files.readString(Paths.get("./src/test/resources/TestPage.html"), StandardCharsets.UTF_8))
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);
        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.shutdown();
    }
    @BeforeEach
    final void beforeEach() {
        Transaction transaction = DB.beginTransaction();
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void testMainPage() {
        HttpResponse<String> response = Unirest
                .get(baseUrl)
                .asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("https://github.com/Azeend");
    }

    @Test
    void testListUrl() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://twitch.com");
        assertThat(body).contains("https://youtube.com");
    }

    @Test
    void testShowUrl() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/1")
                .asString();

        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://twitch.com");
    }

    @Test
    void testAddCorrectUrl() {
        String testPage = mockWebServer.url("/").toString();
        String normalizedTestPage = testPage.substring(0, testPage.length() - 1);

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", testPage)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(normalizedTestPage);
        assertThat(body).contains("Страница успешно добавлена");

        Url addedUrl = new QUrl()
                .name.equalTo(normalizedTestPage)
                .findOne();

        List<Url> urlList = new QUrl()
                .findList();

        assertThat(addedUrl).isNotNull();
        assertThat(addedUrl.getName()).isEqualTo(normalizedTestPage);
        assertThat(urlList.size()).isEqualTo(3);
    }

    @Test
    void testAddIncorrectUrl() {
        String url = "htp/lexteh.ru";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", url)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

        HttpResponse<String> response = Unirest
                .get(baseUrl)
                .asString();

        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("Некорректный URL");
    }
    @Test
    void testAddExistingUrl() {
        String url = "https://youtube.com";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", url)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(url);
        assertThat(body).contains("Страница уже существует");
    }

    @Test
    void testCheckUrl() {
        String testPage = mockWebServer.url("/").toString();
        String normalizedTestPage = testPage.substring(0, testPage.length() - 1);

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", normalizedTestPage)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        List<Url> list = new QUrl()
                .findList();

        assertThat(list.size()).isEqualTo(3);

        HttpResponse responseCheck = Unirest
                .post(baseUrl + "/urls/3/checks")
                .asEmpty();

        assertThat(responseCheck.getStatus()).isEqualTo(302);
        assertThat(responseCheck.getHeaders().getFirst("Location")).isEqualTo("/urls/3");

        HttpResponse<String> responseShow = Unirest
                .get(baseUrl + "/urls/3")
                .asString();
        String body = responseShow.getBody();

        assertThat(responseShow.getStatus()).isEqualTo(200);

        assertThat(body).contains("Test title");
        assertThat(body).contains("Test");
    }
}
