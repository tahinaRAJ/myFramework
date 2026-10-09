package test.example2;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.framework.annotation.Controller;
import dev.framework.annotation.Rest;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.ViewUtil;
import jakarta.servlet.http.HttpServletRequest;
import test.example2.entity.Product;
import test.example2.model.Event;

/**
 * Exemples du binding (BindParam). Ouvre /bind/form pour tester chaque cas avec un formulaire.
 */
@Controller("/bind")
public class BindExampleController {

    @UrlMapping(value = "/form", method = "GET")
    public ViewUtil form() {
        ViewUtil view = new ViewUtil();
        view.setView("bindform");
        return view;
    }

    // EXEMPLE 1 : un objet construit depuis le formulaire (champs name, price -> setName, setPrice)
    @UrlMapping(value = "/product", method = "POST")
    public String product(Product p) {
        return "Produit reçu : name=" + p.getName() + ", price=" + p.getPrice();
    }

    // EXEMPLE 2 : paramètres simples lus par leur NOM Java (String, int, boolean)
    // ex: /bind/hello?name=Rina&age=20&vip=true
    @UrlMapping(value = "/hello", method = "GET")
    public String hello(String name, int age, boolean vip) {
        return "Bonjour " + name + ", " + age + " ans, vip=" + vip;
    }

    // EXEMPLE 3 : deux entiers (absents -> 0)   ex: /bind/sum?a=3&b=4
    @UrlMapping(value = "/sum", method = "GET")
    public String sum(int a, int b) {
        return a + " + " + b + " = " + (a + b);
    }

    // EXEMPLE 4 : LocalDate + enum + int dans un objet, réponse JSON avec @Rest
    // (date=2026-10-31, level=HIGH)
    @UrlMapping(value = "/event", method = "POST")
    @Rest
    public Map<String, Object> event(Event e) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("title", e.getTitle());
        res.put("attendees", e.getAttendees());
        res.put("date", String.valueOf(e.getDate()));
        res.put("level", String.valueOf(e.getLevel()));
        return res;
    }

    // EXEMPLE 5 : mélange HttpServletRequest + objet + paramètre simple
    @UrlMapping(value = "/mix", method = "POST")
    public String mix(HttpServletRequest request, Product p, int quantity) {
        return request.getMethod() + " : " + quantity + " x " + p.getName()
                + " = " + (quantity * p.getPrice());
    }
}
