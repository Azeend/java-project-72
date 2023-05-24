package hexlet.code;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;

public class App {
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(8080);
    }
    public static Javalin getApp() {
        Javalin app = Javalin.create(JavalinConfig::enableDevLogging)
                .get("/", ctx -> ctx.result("Hello World"));
        return app;
    }
    public static String forTest() {
        return "Im worked";
    }
}
