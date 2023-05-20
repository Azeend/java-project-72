package hexlet.code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    public void test() {
        var expected = "Im worked";
        assertEquals(expected, App.forTest());
    }
}
