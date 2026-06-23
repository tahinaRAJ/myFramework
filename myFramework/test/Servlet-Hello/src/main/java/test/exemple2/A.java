package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/helloA")
public class A
 {

    @UrlMapping("/worldA")
    public String world() {
        return "Hello world !";
    }
}