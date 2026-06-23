package test.example2;

import dev.framework.annotation.Controller;

@Controller("/helloA")
public class A
 {

    @Controller("/worldA")
    public String world() {
        return "Hello world !";
    }
}