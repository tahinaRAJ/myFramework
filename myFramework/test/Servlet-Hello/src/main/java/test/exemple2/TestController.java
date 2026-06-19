package test.example2;

import dev.framework.annotation.Controller;

@Controller("/hello2")
public class TestController {

    @Controller("/world2")
    public String world() {
        return "Hello world !";
    }
}