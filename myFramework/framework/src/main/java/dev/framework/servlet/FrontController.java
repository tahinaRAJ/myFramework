package dev.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import dev.framework.util.LoadClass;
import dev.framework.util.Mapping;
import dev.framework.util.UrlMethod;
import dev.framework.util.ViewUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    Map<UrlMethod, Mapping> routesWithMethod;
    String viewPrefix;
    String viewSuffix;
    ApplicationContext springContext;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        super.init();
        routesWithMethod = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("routesWithMethod");
        viewPrefix       = (String)                  getServletContext().getAttribute("prefix");
        viewSuffix       = (String)                  getServletContext().getAttribute("suffix");
        springContext    = (ApplicationContext)       getServletContext().getAttribute("springContext");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        UrlMethod urlMethod = new UrlMethod(pathInfo, request.getMethod());

        if (LoadClass.isARouteInsideMappingWithMethod(urlMethod, routesWithMethod)) {
            Mapping mapping = routesWithMethod.get(urlMethod);
            System.out.println("Route trouvée : " + urlMethod + " -> " + mapping);

            try {
                // Le controller est récupéré depuis le contexte Spring (et non plus via
                // reflection newInstance()) : Spring instancie le bean et injecte au passage
                // tous ses @Autowired (Service, Repository, etc.)
                Object controller = springContext.getBean(mapping.getControllerClass());

                Method controllerMethod = mapping.getMethod();
                Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> paramType = parameterTypes[i];
                    if (paramType.equals(ApplicationContext.class)) {
                        parameters[i] = springContext;
                    } else if (paramType.equals(HttpServletRequest.class)) {
                        parameters[i] = request;
                    } else if (paramType.equals(HttpServletResponse.class)) {
                        parameters[i] = response;
                    } else {
                        parameters[i] = null;
                    }
                }

                Object result = controllerMethod.invoke(controller, parameters);

                // CAS 1 : ViewUtil → forward vers JSP
                if (result instanceof ViewUtil mav) {
                    for (Map.Entry<String, java.util.List<?>> en : mav.getValues().entrySet()) {
                        request.setAttribute(en.getKey(), en.getValue());
                    }
                    if (mav.getView() != null && !mav.getView().isBlank()) {
                        String viewPath = viewPrefix + mav.getView() + viewSuffix;
                        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
                        dispatcher.forward(request, response);
                        return;
                    }
                    throw new ServletException("Aucune vue définie pour " + urlMethod);
                }

                // CAS 2 : String → texte brut
                if (result instanceof String text) {
                    response.setContentType("text/plain;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Resultat de la methode:\n");
                        out.println(text);
                    }
                    return;
                }

                throw new ServletException(
                        "Type de retour non supporté pour " + urlMethod + " : " + result.getClass().getName());

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Impossible d'exécuter la méthode liée à " + urlMethod, e);
            }

        } else {
            // Route non trouvée → lister les routes disponibles
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("Aucune route trouvée pour l'URL : " + pathInfo);
                routesWithMethod.forEach((urlMethodKey, mapping) -> {
                    out.println(urlMethodKey + " -> " + mapping.getClassName() + "->" + mapping.getMethod().getName() + "()");
                });
            }
        }
    }
}
