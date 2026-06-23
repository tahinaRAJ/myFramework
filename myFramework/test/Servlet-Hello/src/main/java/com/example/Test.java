package test.example;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/test")
public class Test
 {

    @UrlMapping("/world")
    public String world() {
        return "Hello world !";
    }

    @UrlMapping("/worldtest")
    public String worldtest() {
        return "Hello world !";
    }
}