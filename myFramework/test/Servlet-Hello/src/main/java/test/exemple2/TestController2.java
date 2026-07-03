package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/hello3")
public class TestController2 {

    @UrlMapping(value = "/world3", method = "GET")
    public String getWorld() {
        return "GET - Hello from TestController2 !";
    }
}
