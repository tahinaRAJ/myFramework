package test.example2;

import java.util.Map;

import dev.framework.annotation.Auth;
import dev.framework.annotation.Controller;
import dev.framework.annotation.Rest;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.Injection;

/**
 * Exemple d'utilisation de @Auth (sprint 10 / 10-bis).
 * L'utilisateur est "connecté" quand la session contient l'attribut "user", et son rôle
 * est dans l'attribut "role" (noms définis dans le web.xml : auth.user-session-key
 * et auth.role-session-key).
 *
 *   GET /auth/public                         -> accessible à tous
 *   GET /auth/profil                         -> @Auth : 401 si non connecté
 *   GET /auth/api/me                         -> @Auth + @Rest : infos de l'utilisateur en JSON
 *   GET /auth/admin                          -> @Auth(roles = "admin") : 403 si le rôle n'est pas admin
 *   GET /auth/gestion                        -> @Auth(roles = {"admin", "manager"})
 *   GET /auth/login?user=rakoto&role=admin   -> met "user" et "role" en session (connexion)
 *   GET /auth/logout                         -> retire "user" et "role" de la session (déconnexion)
 */
@Controller("/auth")
public class AuthController {

    @UrlMapping(value = "/public", method = "GET")
    public String pagePublique() {
        return "Page publique : pas besoin d'être connecté.";
    }

    @UrlMapping(value = "/login", method = "GET")
    public String login(String user, String role, Injection session) {
        if (user == null || user.isBlank()) {
            return "Précise un utilisateur : /auth/login?user=rakoto&role=admin";
        }
        session.injectSessionAttribute("user", user);
        if (role == null || role.isBlank()) {
            session.removeSessionAttribute("role");
            return "Connecté en tant que " + user + " (sans rôle)";
        }
        session.injectSessionAttribute("role", role);
        return "Connecté en tant que " + user + " (rôle : " + role + ")";
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
        Object role = session.getSessionAttribute("role");
        return Map.of("user", session.getSessionAttribute("user"), "role", role == null ? "" : role);
    }

    @UrlMapping(value = "/admin", method = "GET")
    @Auth(roles = "admin")
    public String admin(Injection session) {
        return "Espace admin : bonjour " + session.getSessionAttribute("user") + ".";
    }

    @UrlMapping(value = "/gestion", method = "GET")
    @Auth(roles = { "admin", "manager" })
    public String gestion(Injection session) {
        return "Espace gestion (admin ou manager) : " + session.getSessionAttribute("user")
                + " a le rôle " + session.getSessionAttribute("role") + ".";
    }

    @UrlMapping(value = "/logout", method = "GET")
    public String logout(Injection session) {
        session.removeSessionAttribute("user");
        session.removeSessionAttribute("role");
        return "Déconnecté";
    }
}
