package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.ViewUtil;
import jakarta.servlet.http.HttpServletRequest;
import test.example2.entity.Product;

/** Exemples du binding simple (BindParam). Ouvre /bind/form. */
@Controller("/bind")
public class BindExampleController {

    @UrlMapping(value = "/form", method = "GET")
    public ViewUtil form() {
        ViewUtil view = new ViewUtil();
        view.setView("bindform");
        return view;
    }

    // EXEMPLE 1 : un objet construit depuis le formulaire (name, price)
    @UrlMapping(value = "/product", method = "POST")
    public String product(Product p) {
        return "Produit reçu : name=" + p.getName() + ", price=" + p.getPrice();
    }

    // EXEMPLE 2 : objet + HttpServletRequest dans la même méthode
    @UrlMapping(value = "/mix", method = "POST")
    public String mix(HttpServletRequest request, Product p) {
        return request.getMethod() + " : " + p.getName() + " à " + p.getPrice();
    }
}
