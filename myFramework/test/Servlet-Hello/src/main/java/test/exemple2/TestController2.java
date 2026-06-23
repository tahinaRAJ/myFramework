package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/hello3")
public class TestController2 {

    @UrlMapping("/world3")
    public String world() {
        return "Hello world !";
    }
}