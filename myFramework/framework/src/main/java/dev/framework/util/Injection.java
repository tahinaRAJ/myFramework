package dev.framework.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Accès à la session HTTP depuis un controller, sans dépendre de l'API Servlet.
 *
 * Déclarée comme bean singleton dans l'ApplicationContext Spring, avec une HashMap
 * (bean "sessionMap") : le FrontController la récupère via springContext.getBean(Injection.class),
 * la remplit avec les attributs de la HttpSession, puis recopie la map dans la HttpSession
 * après l'exécution de la méthode du controller.
 */
public class Injection {
    Map<String, Object> session;

    public Injection() {
        this.session = new HashMap<>();
    }

    public Map<String, Object> getSession() {
        return session;
    }

    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public void injectSession(Map<String, Object> session) {
        this.session = session;
    }

    public void injectSessionAttribute(String key, Object value) {
        if (session != null) {
            session.put(key, value);
        }
    }

    public Object getSessionAttribute(String key) {
        if (session != null) {
            return session.get(key);
        }
        return null;
    }

    public void removeSessionAttribute(String key) {
        if (session != null) {
            session.remove(key);
        }
    }

}
