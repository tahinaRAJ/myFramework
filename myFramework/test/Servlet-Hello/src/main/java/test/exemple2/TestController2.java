package test.example2;

import dev.framework.annotation.Controller;

@Controller("/hello3")
public class TestController2 {

    @Controller("/world3")
    public String world() {
        return "Hello world !";
    }
}