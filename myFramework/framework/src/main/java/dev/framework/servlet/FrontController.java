package dev.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.LoadClass;
import dev.framework.util.Mapping;
import dev.framework.util.UrlMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    // UrlMethod comme clé, Mapping comme valeur
    private final Map<UrlMethod, Mapping> routes = new HashMap<>();
    private final Set<String> scannedPackages = new LinkedHashSet<>();
    private final Set<String> scannedClasses  = new LinkedHashSet<>();

    @Override
    public void init() throws ServletException {
        try {
            String basePackage = getInitParameter("basePackage");
            List<Class<?>> controllers;

            if (basePackage != null && !basePackage.isEmpty()) {
                controllers = LoadClass.getControllers(basePackage);
            } else {
                controllers = LoadClass.getControllers();
            }

            for (Class<?> clazz : controllers) {
                scannedPackages.add(clazz.getPackageName());
                scannedClasses.add(clazz.getName());

                Controller classAnnotation = clazz.getDeclaredAnnotation(Controller.class);
                String basePath = (classAnnotation != null) ? classAnnotation.value() : "";

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping urlMapping  = method.getDeclaredAnnotation(UrlMapping.class);
                        String     methodPath  = urlMapping.value();
                        String     httpMethod  = urlMapping.method().toUpperCase();
                        String     fullPath    = basePath + methodPath;

                        UrlMethod key     = new UrlMethod(fullPath, httpMethod);
                        Mapping   mapping = new Mapping(clazz, method);
                        if (routes.containsKey(key)) {
                            Mapping existing = routes.get(key);
                            throw new ServletException(
                                "[ERREUR DOUBLON] " + httpMethod + " " + fullPath
                                + " est declaree dans "
                                + existing.getControllerClass().getName() + "." + existing.getMethod().getName()
                                + " ET "
                                + clazz.getName() + "." + method.getName()
                            );
                        }
                        routes.put(key, mapping);

                        System.out.println("[Framework] Route enregistrée : "
                                + httpMethod + " " + fullPath
                                + " → " + mapping);
                    }
                }
            }

        } catch (Exception e) {
            throw new ServletException("Erreur init FrontController", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response, "POST");
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response,
            String httpMethod) throws IOException {

        String uri      = request.getRequestURI();
        String ctxPath  = request.getContextPath();
        String path     = uri.substring(ctxPath.length());

        UrlMethod key    = new UrlMethod(path, httpMethod);
        Mapping   mapping = routes.get(key);

        PrintWriter out = response.getWriter();

        if (mapping == null) {
            boolean found = false;
            for (Map.Entry<UrlMethod, Mapping> entry : routes.entrySet()) {
                // chercher par URL uniquement — affiche GET et POST
                if (entry.getKey().getUrl().startsWith(path)) {
                    found = true;
                    out.println(entry.getKey().getMethod() + " " + entry.getKey().getUrl()
                            + " -> " + entry.getValue());
                }
            }
            if (!found) {
                out.println("=== Routes disponibles ===");
                for (Map.Entry<UrlMethod, Mapping> entry : routes.entrySet()) {
                    out.println(entry.getKey().getMethod() + " " + entry.getKey().getUrl()
                            + " -> " + entry.getValue());
                }
            }
            return;
        }
        

        // Route trouvée → invoke via Mapping
        try {
            Object instance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
            Object result   = mapping.getMethod().invoke(instance);
            out.println(result);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Erreur : " + e.getMessage());
        }
    }
}