package test.example2;

import java.util.Map;

import dev.framework.annotation.Auth;
import dev.framework.annotation.Controller;
import dev.framework.annotation.Rest;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.Injection;

/**
 * Exemple d'utilisation de @Auth (sprint 10).
 * L'utilisateur est "connecté" quand la session contient l'attribut "user"
 * (nom défini dans le web.xml : context-param auth.user-session-key).
 *
 *   GET /auth/public              -> accessible à tous
 *   GET /auth/profil              -> @Auth : 401 si non connecté
 *   GET /auth/api/me              -> @Auth + @Rest : infos de l'utilisateur en JSON
 *   GET /auth/login?user=rakoto   -> met "user" en session (connexion)
 *   GET /auth/logout              -> retire "user" de la session (déconnexion)
 */
@Controller("/auth")
public class AuthController {

    @UrlMapping(value = "/public", method = "GET")
    public String pagePublique() {
        return "Page publique : pas besoin d'être connecté.";
    }

    @UrlMapping(value = "/login", method = "GET")
    public String login(String user, Injection session) {
        if (user == null || user.isBlank()) {
            return "Précise un utilisateur : /auth/login?user=rakoto";
        }
        session.injectSessionAttribute("user", user);
        return "Connecté en tant que " + user;
    }

    @UrlMapping(value = "/profil", method = "GET")
    @Auth
    public String profil(Injection session) {
        return "Page privée : bienvenue " + session.getSessionAttribute("user") + " !";
    }

    @UrlMapping(value = "/api/me", method = "GET")
    @Auth
    @Rest
    public Map<String, Object> me(Injection session) {
        return Map.of("user", session.getSessionAttribute("user"), "authentifie", true);
    }

    @UrlMapping(value = "/logout", method = "GET")
    public String logout(Injection session) {
        session.removeSessionAttribute("user");
        return "Déconnecté";
    }
}
