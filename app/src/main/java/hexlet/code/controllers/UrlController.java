package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;

import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public final class UrlController {
    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler addUrl = ctx -> {
        try {
            URL url = new URL(Objects.requireNonNull(ctx.formParam("url")));

            String normalizedUrl = url.getProtocol() + "://" + url.getAuthority();
            boolean existedUrl = new QUrl()
                    .name.iequalTo(normalizedUrl)
                    .exists();

            if (existedUrl) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/urls");
            } else {
                new Url(normalizedUrl).save();
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flash-type", "success");
                ctx.redirect("/urls");
            }
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
        }
    };
    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };
    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        HttpResponse<String> response;

        try {
            response = Unirest.get(url.getName()).asString();

            int statusCode = response.getStatus();

            Document body = Jsoup.parse(response.getBody());

            String title = body.title();

            Element h1 = body.selectFirst("h1");
            Element description = body.selectFirst("meta[name=description]");
            String head1 = (h1 != null) ? h1.text() : "";
            String description1 = (description != null) ? description.attr("content") : "";

            UrlCheck urlCheck = new UrlCheck(description1, title, head1, statusCode, url);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Страница недоступна");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };
}
