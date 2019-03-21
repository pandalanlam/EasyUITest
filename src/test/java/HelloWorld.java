import com.codeborne.selenide.Configuration;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.*;


public class HelloWorld {

    @Test
    public void hello() {
        Configuration.browser = "Chrome";
        Configuration.baseUrl="http://www.baidu.com";
        open("/");
    }

}
