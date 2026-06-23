package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/hello2")
public class TestController {

    @UrlMapping("/world2")
    public String world() {
        return "Hello world !";
    }
}