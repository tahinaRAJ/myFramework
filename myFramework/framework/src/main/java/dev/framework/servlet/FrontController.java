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
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    private final Map<String, Method> routes = new HashMap<>();
    private final Map<String, Object> instances = new HashMap<>();
    private final Set<String> scannedPackages = new LinkedHashSet<>();
    private final Set<String> scannedClasses = new LinkedHashSet<>(); 
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
                scannedPackages.add(clazz.getPackageName()); // une seule boucle suffit
                scannedClasses.add(clazz.getName()); // ajoute le nom de la classe

                Controller classAnnotation = clazz.getDeclaredAnnotation(Controller.class);
                String basePath = (classAnnotation != null) ? classAnnotation.value() : "";
                Object instance = clazz.getDeclaredConstructor().newInstance();

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping methodAnnotation = method.getDeclaredAnnotation(UrlMapping.class);
                        String methodPath = (methodAnnotation != null) ? methodAnnotation.value() : "";
                        String fullPath = basePath + methodPath;

                        routes.put(fullPath, method);
                        instances.put(fullPath, instance);

                        System.out.println("[Framework] Route enregistrée : " + fullPath
                                + " → " + clazz.getSimpleName() + "." + method.getName() + "()");
                    }
                }
            }

            System.out.println("[Framework] Packages scannés contenant des contrôleurs :");
            for (String pkg : scannedPackages) {
                System.out.println("  - " + pkg);
            }

        } catch (Exception e) {
            throw new ServletException("Erreur init FrontController", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
    String path = uri.substring(contextPath.length());

    Method method = routes.get(path);

    if (method == null) {
        // URL partielle → chercher les routes qui commencent par ce path
        PrintWriter out = response.getWriter();
        boolean found = false;
        for (Map.Entry<String, Method> entry : routes.entrySet()) {
            if (entry.getKey().startsWith(path)) {
                found = true;
                out.println(entry.getKey()
                    + " -> " + entry.getValue().getDeclaringClass().getName()
                    + "." + entry.getValue().getName());
            }
        }
        // Rien trouvé avec ce préfixe → afficher toutes les routes
        if (!found) {
            out.println("=== Routes disponibles ===");
            for (Map.Entry<String, Method> entry : routes.entrySet()) {
                out.println(entry.getKey()
                    + " -> " + entry.getValue().getDeclaringClass().getName()
                    + "." + entry.getValue().getName());
            }
        }
        return;
    }

    try {
        response.getWriter().println("route trouvee : " + path
            + " -> " + method.getDeclaringClass().getName()
            + "." + method.getName());
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("Erreur : " + e.getMessage());
    }
}

    // public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //     String uri = request.getRequestURI();
    //     String contextPath = request.getContextPath();
    //     String path = uri.substring(contextPath.length()); // enlève "/MyFramework"

    //     Method method = routes.get(path);

    //     // if (method == null) {
    //     //     response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    //     //     // response.getWriter().println("404 - Aucune route pour : " + path);
    //     //     response.getWriter().println("Classes scannees :");
    //     //     // for (String pkg : scannedPackages) {
    //     //         for (String cls : scannedClasses) {
    //     //         response.getWriter().println("  - " + cls);
    //     //     // }
    //     //     }
    //     //     return;
    //     // }

    //     if (method == null) {
    //         response.setContentType("text/plain");
    //         PrintWriter out = response.getWriter();

    //         out.println("=== Routes disponibles ===");
    //         for (Map.Entry<String, Method> entry : routes.entrySet()) {
    //             String url = entry.getKey();
    //             Method m = entry.getValue();
    //             out.println(url + " -> " + m.getDeclaringClass().getName() + "." + m.getName() + "()");
    //         }
    //         return;
    //     }

    //     try {
    //         response.getWriter().println("route trouvee : " + path
    //         + " -> " + method.getDeclaringClass().getName() 
    //         + "." + method.getName());
    //         // Object result = method.invoke(instance);

    //         // System.out.println("[Framework] Résultat : " + result);
    //         // response.getWriter().println(result); // on remet l'affichage du résultat de la route
    //     } catch (Exception e) {
    //         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    //         response.getWriter().println("Erreur : " + e.getMessage());
    //     }
    // }
}