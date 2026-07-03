package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/hello2")
public class TestController {

    @UrlMapping(value = "/world2", method = "GET")
    public String getWorld() {
        return "GET - Hello from TestController !";
    }
}
