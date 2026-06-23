package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/helloB")
public class B {

    @UrlMapping("/worldB")
    public String world() {
        return "Hello world !";
    }
}