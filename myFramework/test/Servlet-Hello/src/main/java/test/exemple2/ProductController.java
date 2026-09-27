package test.example2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import dev.framework.annotation.Controller;
import dev.framework.annotation.Rest;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.ViewUtil;
import jakarta.servlet.http.HttpServletRequest;
import test.example2.entity.Product;
import test.example2.service.ProductService;

/**
 * Démonstration complète : Controller (framework maison) -> Service -> Repository (Spring Data JPA) -> Entity.
 * Le ProductService est injecté par Spring car @Controller est méta-annoté @Component,
 * et ce controller est donc lui-même un bean géré par le contexte Spring.
 */
@Controller("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @UrlMapping(value = "/list", method = "GET")
    public ViewUtil list() {
        ViewUtil view = new ViewUtil();
        view.setView("products");
        view.addValue("products", productService.findAll());
        return view;
    }

    /**
     * Même donnée que /products/list, mais renvoyée en JSON grâce à @Rest,
     * au lieu d'être forwardée vers une JSP.
     */
    @UrlMapping(value = "/api", method = "GET")
    @Rest
    public List<Product> apiList() {
        return productService.findAll();
    }

    @UrlMapping(value = "/add", method = "POST")
    public ViewUtil add(HttpServletRequest request) {
        String name = request.getParameter("name");
        double price = Double.parseDouble(request.getParameter("price"));
        productService.save(name, price);

        ViewUtil view = new ViewUtil();
        view.setView("products");
        view.addValue("products", productService.findAll());
        return view;
    }

    @UrlMapping(value = "/delete", method = "POST")
    public ViewUtil delete(HttpServletRequest request) {
        Long id = Long.parseLong(request.getParameter("id"));
        productService.deleteById(id);

        ViewUtil view = new ViewUtil();
        view.setView("products");
        view.addValue("products", productService.findAll());
        return view;
    }
}
