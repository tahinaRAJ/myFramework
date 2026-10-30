package test.example2;

import java.util.concurrent.atomic.AtomicInteger;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.ViewUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Exemple de controller singleton :
 *   GET /compteur/voir -> incrémente le compteur et l'affiche
 *
 * Le compteur est un champ du controller : comme il n'existe qu'une seule instance,
 * sa valeur est partagée par tous les utilisateurs, même depuis un autre navigateur.
 * AtomicInteger car l'instance est utilisée par plusieurs requêtes en même temps.
 */
@Controller("/compteur")
public class CompteurController {

    private final AtomicInteger compteur = new AtomicInteger();

    @UrlMapping(value = "/voir", method = "GET")
    public ViewUtil voir(HttpServletRequest request) {
        ViewUtil view = new ViewUtil();
        view.setView("compteur");
        view.addValue("compteur", String.valueOf(compteur.incrementAndGet()));
        view.addValue("instance", String.valueOf(System.identityHashCode(this)));
        view.addValue("sessionId", request.getSession().getId());
        return view;
    }
}
