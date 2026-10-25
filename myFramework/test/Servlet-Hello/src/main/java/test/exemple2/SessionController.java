package test.example2;

import dev.framework.annotation.Controller;
import dev.framework.annotation.Rest;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.Injection;

/**
 * Exemple d'utilisation de la session via Injection :
 *   GET /session/login?user=rakoto   -> met "user" en session
 *   GET /session/visit               -> incrémente un compteur de visites en session
 *   GET /session/show                -> affiche le contenu de la session (texte brut)
 *   GET /session/json                -> même contenu, en JSON (@Rest)
 *   GET /session/logout              -> retire "user" et "visits" de la session
 */
@Controller("/session")
public class SessionController {

    @UrlMapping(value = "/login", method = "GET")
    public String login(String user, Injection session) {
        session.injectSessionAttribute("user", user);
        return "Connecté en tant que " + user;
    }

    @UrlMapping(value = "/visit", method = "GET")
    public String visit(Injection session) {
        Integer visits = (Integer) session.getSessionAttribute("visits");
        visits = visits == null ? 1 : visits + 1;
        session.injectSessionAttribute("visits", visits);
        return "Visite n°" + visits + " (utilisateur : " + session.getSessionAttribute("user") + ")";
    }

    @UrlMapping(value = "/show", method = "GET")
    public Injection show(Injection session) {
        return session;
    }

    @UrlMapping(value = "/json", method = "GET")
    @Rest
    public Object json(Injection session) {
        return session.getSession();
    }

    @UrlMapping(value = "/logout", method = "GET")
    public String logout(Injection session) {
        session.removeSessionAttribute("user");
        session.removeSessionAttribute("visits");
        return "Déconnecté";
    }
}
