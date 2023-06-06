package hexlet.code;

import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlController;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class App {
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8080");
        return Integer.valueOf(port);
    }


    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);

        app.routes(() -> {
            path("urls", () -> {
                get(UrlController.listUrls);
                post(UrlController.addUrl);
                path("{id}", () -> {
                    get(UrlController.showUrl);
                    post("checks", UrlController.checkUrl);
                });
            });
        });
    }
    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
            JavalinThymeleaf.init(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });
        return app;
    }
    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }
}
