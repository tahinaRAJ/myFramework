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
                scannedPackages.add(clazz.getPackageName());
                scannedClasses.add(clazz.getName());

                Controller classAnnotation = clazz.getDeclaredAnnotation(Controller.class);
                String basePath = (classAnnotation != null) ? classAnnotation.value() : "";
                Object instance = clazz.getDeclaredConstructor().newInstance();

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping methodAnnotation = method.getDeclaredAnnotation(UrlMapping.class);
                        String methodPath = methodAnnotation.value();
                        String httpMethod = methodAnnotation.method().toUpperCase();
                        String fullPath = basePath + methodPath;
                        String routeKey = httpMethod + ":" + fullPath;

                        routes.put(routeKey, method);
                        instances.put(routeKey, instance);

                        System.out.println("[Framework] Route enregistrée : "
                                + httpMethod + " " + fullPath
                                + " → " + clazz.getSimpleName() + "." + method.getName() + "()");
                    }
                }
            }

            System.out.println("[Framework] Packages scannés :");
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
        processRequest(request, response, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response, "POST");
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response,
            String httpMethod) throws IOException {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());
        String routeKey = httpMethod + ":" + path;

        Method method = routes.get(routeKey);

        if (method == null) {
            PrintWriter out = response.getWriter();
            boolean found = false;
            for (Map.Entry<String, Method> entry : routes.entrySet()) {
                if (entry.getKey().startsWith(httpMethod + ":" + path)) {
                    found = true;
                    String key = entry.getKey(); // "GET:/helloA/worldA"
                    out.println(key.replace(":", " ")
                            + " -> " + entry.getValue().getDeclaringClass().getName()
                            + "." + entry.getValue().getName());
                }
            }
            if (!found) {
                out.println("=== Routes disponibles ===");
                for (Map.Entry<String, Method> entry : routes.entrySet()) {
                    String key = entry.getKey();
                    out.println(key.replace(":", " ")
                            + " -> " + entry.getValue().getDeclaringClass().getName()
                            + "." + entry.getValue().getName());
                }
            }
            return;
        }

        // Route trouvée → invoke
        try {
            Object instance = instances.get(routeKey);
            Object result = method.invoke(instance);
            response.getWriter().println(result);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Erreur : " + e.getMessage());
        }
    }
}
