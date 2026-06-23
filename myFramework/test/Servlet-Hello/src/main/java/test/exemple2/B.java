package test.example2;

import dev.framework.annotation.Controller;

@Controller("/helloB")
public class B {

    @Controller("/worldB")
    public String world() {
        return "Hello world !";
    }
}