package dev.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import dev.framework.util.Mapping;
import dev.framework.util.UrlMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private Map<UrlMethod, Mapping> routes;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        List<String> errors = (List<String>) getServletContext().getAttribute("frameworkErrors");

        if (errors != null && !errors.isEmpty()) {
            // Listener a détecté des erreurs — on ne charge rien
            System.err.println("[FrontController] Erreurs détectées — chargement annulé");
            return;
        }

        routes = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("routes");
        System.out.println("[FrontController] " + routes.size() + " route(s) chargée(s) — prêt");
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

        // routes null → erreur détectée au démarrage → 404 natif Tomcat
        if (routes == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        PrintWriter out = response.getWriter();
        String uri      = request.getRequestURI();
        String ctxPath  = request.getContextPath();
        String path     = uri.substring(ctxPath.length());

        UrlMethod key     = new UrlMethod(path, httpMethod);
        Mapping   mapping = routes.get(key);

        if (mapping == null) {
            boolean found = false;
            for (Map.Entry<UrlMethod, Mapping> entry : routes.entrySet()) {
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
