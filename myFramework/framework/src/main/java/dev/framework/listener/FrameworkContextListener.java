package dev.framework.listener;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.LoadClass;
import dev.framework.util.Mapping;
import dev.framework.util.UrlMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String basePackage = ctx.getInitParameter("basePackage");

        Map<UrlMethod, Mapping> routes = new HashMap<>();
        List<String> errors = new ArrayList<>();

        System.out.println("[Listener] Démarrage de la vérification...");

        try {
            List<Class<?>> controllers;
            if (basePackage != null && !basePackage.isEmpty()) {
                controllers = LoadClass.getControllers(basePackage);
            } else {
                controllers = LoadClass.getControllers();
            }

            for (Class<?> clazz : controllers) {
                Controller classAnnotation = clazz.getDeclaredAnnotation(Controller.class);
                String basePath = (classAnnotation != null) ? classAnnotation.value() : "";

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping urlMapping = method.getDeclaredAnnotation(UrlMapping.class);
                        String     httpMethod = urlMapping.method().toUpperCase();
                        String     fullPath   = basePath + urlMapping.value();
                        UrlMethod  key        = new UrlMethod(fullPath, httpMethod);
                        Mapping    mapping    = new Mapping(clazz, method);

                        if (routes.containsKey(key)) {
                            // Doublon — on enregistre et on STOPPE tout
                            Mapping existing = routes.get(key);
                            String error = "[ERREUR DOUBLON] " + httpMethod + " " + fullPath
                                    + " declaree dans "
                                    + existing.getControllerClass().getName() + "." + existing.getMethod().getName()
                                    + " ET "
                                    + clazz.getName() + "." + method.getName();
                            errors.add(error);
                            System.err.println("[Listener] " + error);
                            // Ne pas continuer à lire le reste
                            ctx.setAttribute("frameworkErrors", errors);
                            ctx.setAttribute("routes", null);
                            return;
                        }

                        routes.put(key, mapping);
                        System.out.println("[Listener] Route OK : " + httpMethod + " " + fullPath + " → " + mapping);
                    }
                }
            }

        } catch (Exception e) {
            errors.add("[ERREUR CRITIQUE] " + e.getMessage());
            System.err.println("[Listener] Erreur critique : " + e.getMessage());
            ctx.setAttribute("frameworkErrors", errors);
            ctx.setAttribute("routes", null);
            return;
        }

        // Aucune erreur — tout est OK
        ctx.setAttribute("routes", routes);
        ctx.setAttribute("frameworkErrors", errors);
        System.out.println("[Listener] Vérification terminée — " + routes.size() + " route(s), aucune erreur");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[Listener] Arrêt du framework");
    }
}
