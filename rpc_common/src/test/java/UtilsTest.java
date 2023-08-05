import org.junit.jupiter.api.Test;

import java.net.URL;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-26 20:12
 */
public class UtilsTest {
    @Test
    public void readPropertiesTest() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        System.out.println(url);
    }
}
