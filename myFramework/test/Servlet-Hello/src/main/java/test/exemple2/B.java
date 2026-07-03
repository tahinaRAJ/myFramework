package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/helloB")
public class B {

    @UrlMapping(value = "/worldB", method = "GET")
    public String getWorld() {
        return "GET - Hello from B !";
    }

    @UrlMapping(value = "/worldB", method = "POST")
    public String postWorld() {
        return "POST - Hello from B !";
    }
}
