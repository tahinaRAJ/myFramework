package dev.framework.util;
import java.util.Map;

public class Injection {
    Map<String, Object> session;

    public Injection() {
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
