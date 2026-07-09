package dev.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import dev.framework.util.Mapping;
import dev.framework.util.UrlMethod;
import dev.framework.util.ViewUtil; 
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private Map<UrlMethod, Mapping> routes;
    private String viewPrefix;
    private String viewSuffix;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        // 1. Sécurité initiale de ta version 1
        List<String> errors = (List<String>) getServletContext().getAttribute("frameworkErrors");

        if (errors != null && !errors.isEmpty()) {
            System.err.println("[FrontController] Erreurs détectées au démarrage — chargement annulé");
            return;
        }

        routes = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("routes");
        
        // 2. Récupération des configurations de dossier pour les vues (comme la v2)
        viewPrefix = getServletContext().getInitParameter("view.prefix");
        viewSuffix = getServletContext().getInitParameter("view.suffix");

        System.out.println("[FrontController] " + (routes != null ? routes.size() : 0) + " route(s) chargée(s) — prêt");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response, "POST");
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response, String httpMethod) 
            throws ServletException, IOException {

        // Si routes null → erreur détectée au démarrage → 404 natif
        if (routes == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String uri     = request.getRequestURI();
        String ctxPath = request.getContextPath();
        String path    = uri.substring(ctxPath.length());

        UrlMethod key     = new UrlMethod(path, httpMethod);
        Mapping   mapping = routes.get(key);

        // --- GESTION DES ROUTES INTROUVABLES (Fallback & Debug) ---
        if (mapping == null) {
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                boolean found = false;
                for (Map.Entry<UrlMethod, Mapping> entry : routes.entrySet()) {
                    if (entry.getKey().getUrl().startsWith(path)) {
                        found = true;
                        out.println(entry.getKey().getMethod() + " " + entry.getKey().getUrl() + " -> " + entry.getValue());
                    }
                }
                if (!found) {
                    out.println("=== Routes disponibles ===");
                    for (Map.Entry<UrlMethod, Mapping> entry : routes.entrySet()) {
                        out.println(entry.getKey().getMethod() + " " + entry.getKey().getUrl() + " -> " + entry.getValue());
                    }
                }
            }
            return;
        }

        // --- EXÉCUTION DU CONTRÔLEUR ---
        try {
            Object instance         = mapping.getControllerClass().getDeclaredConstructor().newInstance();
            Method controllerMethod = mapping.getMethod();
            Object result           = controllerMethod.invoke(instance);

            if (result == null) {
                throw new ServletException("La méthode liée à " + key + " a retourné null");
            }

            // CAS 1 : Le contrôleur renvoie un ViewUtil (Redirection vers une JSP/Vue)
            if (result instanceof ViewUtil viewUtil) {
                if (viewUtil.getValues() != null) {
                    // On passe la Map de données à la requête sous le nom "map"
                    request.setAttribute("map", viewUtil.getValues());
                }

                if (viewUtil.getView() != null && !viewUtil.getView().isBlank()) {
                    // Construction du chemin complet (ex: /WEB-INF/views/home.jsp)
                    String viewPath = viewPrefix + viewUtil.getView() + viewSuffix;
                    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
                    dispatcher.forward(request, response);
                    return;
                }

                throw new ServletException("Aucune vue définie dans le ViewUtil pour " + key);
            }

            // CAS 2 : Le contrôleur renvoie une simple String (Affichage de texte brut)
            if (result instanceof String text) {
                response.setContentType("text/plain;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.println(text);
                }
                return;
            }

            // CAS DÉFAUT : Type non supporté
            throw new ServletException("Type de retour non supporté pour " + key + " : " + result.getClass().getName());

        } catch (Exception e) {
            // Log de l'erreur réelle dans la console du serveur pour le debug
            e.printStackTrace(); 
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("Erreur Interne : " + e.getMessage());
            }
        }
    }
}