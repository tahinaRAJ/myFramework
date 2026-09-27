package test.example2;

import javax.swing.text.View;

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
        view.addValue("test", "Hello world !");
        view.addValue("test2", "test du view");
        view.addValue("test3", "test du view 2");
        return view;    
    }
}