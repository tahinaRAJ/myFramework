package test.exemple2;

import javax.swing.text.View;
import java.util.List;
import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.ViewUtil;

@Controller("/test")
public class Test
 {

    @UrlMapping("/world")
    public String world() {
        return "Hello world !";
    }

    @UrlMapping("/worldtest")
    public ViewUtil worldtest() {
        ViewUtil view = new ViewUtil();
        view.setView("worldtest");
        view.addValue("test", List.of("Hello world !"));
        view.addValue("test2", List.of("test du view"));
        view.addValue("test3", List.of("test du view 2"));
        return view;    
    }
}