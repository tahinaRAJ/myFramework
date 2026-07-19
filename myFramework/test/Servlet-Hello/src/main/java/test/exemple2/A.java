package test.example2;

import java.util.List;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.ViewUtil;

@Controller("/helloA")
public class A {

    @UrlMapping(value = "/worldA", method = "GET")
    public ViewUtil worldtest() {
        ViewUtil view = new ViewUtil();
        view.setView("worldtest");
        view.addValue("test",  List.of("Hello world !"));
        view.addValue("test2", List.of("test du view"));
        view.addValue("test3", List.of("test du view 2"));
        return view;    
    }

    @UrlMapping(value = "/worldA", method = "POST")
    public ViewUtil postWorld() {
        ViewUtil view = new ViewUtil();
        view.setView("worldtest");
        view.addValue("test", List.of("POST - Hello from A !"));
        return view;
    }
}
