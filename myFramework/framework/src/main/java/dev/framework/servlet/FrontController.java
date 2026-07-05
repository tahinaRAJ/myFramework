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

        // Si le Listener a détecté des erreurs → HTTP 500, servlet non démarré
        if (errors != null && !errors.isEmpty()) {
            throw new ServletException(String.join("\n", errors));
        }

        routes = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("routes");

        if (routes == null) {
            throw new ServletException("[FrontController] Routes non initialisées. Vérifiez web.xml.");
        }

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

        String uri     = request.getRequestURI();
        String ctxPath = request.getContextPath();
        String path    = uri.substring(ctxPath.length());

        UrlMethod key     = new UrlMethod(path, httpMethod);
        Mapping   mapping = routes.get(key);

        PrintWriter out = response.getWriter();

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

        // Route trouvée → invoke
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
