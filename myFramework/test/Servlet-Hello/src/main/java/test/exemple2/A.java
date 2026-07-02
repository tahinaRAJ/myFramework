package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;

@Controller("/helloA")
public class A {

    @UrlMapping(value = "/worldA", method = "GET")
    public String getWorld() {
        return "GET - Hello from A !";
    }

    @UrlMapping(value = "/worldA", method = "POST")
    public String postWorld() {
        return "POST - Hello from A !";
    }
}
