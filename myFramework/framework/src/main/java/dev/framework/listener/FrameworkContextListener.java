package dev.framework.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import dev.framework.util.LoadClass;
import dev.framework.util.Mapping;
import dev.framework.util.UrlMethod;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class FrameworkContextListener implements ServletContextListener {

    String packageName;
    String viewPrefix;
    String viewSuffix;
    String annotationRest;
    Map<UrlMethod, Mapping> toutesLesRoutes;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[INIT] Tomcat démarre l'application. Lancement du scan des routes...");

        try {
            ApplicationContext springContext = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(sce.getServletContext());

            toutesLesRoutes = new HashMap<>();
            Properties prop = new Properties();
            try (InputStream input = LoadClass.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new RuntimeException("[ERREUR] Impossible de trouver le fichier config.properties.");
                }
                prop.load(input);
                packageName = prop.getProperty("app.package");
                annotationRest = prop.getProperty("annotation.rest");
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de la lecture de config.properties", e);
            }

            LoadClass.loadUrlMappingsWithMethod(packageName, toutesLesRoutes);

            viewPrefix = sce.getServletContext().getInitParameter("view.prefix");
            viewSuffix = sce.getServletContext().getInitParameter("view.suffix");

            sce.getServletContext().setAttribute("routesWithMethod", toutesLesRoutes);
            sce.getServletContext().setAttribute("prefix", viewPrefix);
            sce.getServletContext().setAttribute("suffix", viewSuffix);
            sce.getServletContext().setAttribute("springContext", springContext);
            sce.getServletContext().setAttribute("annotationRest", annotationRest);

            System.out.println("[SUCCESS] Scan terminé avec succès. " + toutesLesRoutes.size() + " routes chargées.");

        } catch (IllegalStateException e) {
            System.err.println("[ERREUR CRITIQUE DÉMARRAGE] " + e.getMessage());
            throw new RuntimeException("Échec du déploiement de l'application à cause d'un conflit de routes.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[SHUTDOWN] L'application s'arrête.");
    }
}
