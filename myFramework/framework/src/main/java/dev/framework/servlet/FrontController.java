package dev.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.framework.annotation.Auth;
import dev.framework.util.*;
// import dev.framework.util.BindParam;
// import dev.framework.util.FileUpload;
// import dev.framework.util.LoadClass;
// import dev.framework.util.Mapping;
// import dev.framework.util.UrlMethod;
// import dev.framework.util.ViewUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class FrontController extends HttpServlet {

    Map<UrlMethod, Mapping> routesWithMethod;
    String viewPrefix;
    String viewSuffix;
    ApplicationContext springContext;

    /** Nom de l'attribut de session qui contient l'utilisateur connecté (web.xml : auth.user-session-key). */
    String userSessionKey;

    /** Classe de l'annotation @Rest (ex: dev.framework.annotation.Rest), résolue une seule fois au démarrage. */
    Class<? extends Annotation> restAnnotationClass;

    final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        super.init();
        routesWithMethod = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("routesWithMethod");
        viewPrefix       = (String)                  getServletContext().getAttribute("prefix");
        viewSuffix       = (String)                  getServletContext().getAttribute("suffix");
        springContext    = (ApplicationContext)       getServletContext().getAttribute("springContext");
        userSessionKey   = (String)                  getServletContext().getAttribute("userSessionKey");

        String annotationRest = (String) getServletContext().getAttribute("annotationRest");
        if (annotationRest != null && !annotationRest.isBlank()) {
            try {
                restAnnotationClass = Class.forName(annotationRest).asSubclass(Annotation.class);
            } catch (ClassNotFoundException e) {
                throw new ServletException("Annotation REST introuvable : " + annotationRest, e);
            }
        }
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
                Method controllerMethod = mapping.getMethod();

                // @Auth : la méthode est réservée aux utilisateurs connectés
                if (controllerMethod.isAnnotationPresent(Auth.class) && !isAuthenticated(request)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                            "Vous devez être connecté pour accéder à " + pathInfo);
                    return;
                }

                // Le controller est récupéré depuis le contexte Spring : Spring l'instancie
                // et injecte au passage tous ses @Autowired (Service, Repository, etc.)
                Object controller = springContext.getBean(mapping.getControllerClass());

                Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = populateParameter(controllerMethod, i, request, response, urlMethod);
                }

                Object result = controllerMethod.invoke(controller, parameters);

                // Les modifications faites par le controller sur la map de session
                // sont recopiées dans la vraie HttpSession
                for (Object parameter : parameters) {
                    if (parameter instanceof Injection injection) {
                        saveSession(injection, request.getSession());
                    }
                }

                // CAS 1 : la méthode est annotée @Rest -> on sérialise le résultat en JSON,
                // quel que soit son type (objet, liste, String...)
                boolean isRest = restAnnotationClass != null
                        && controllerMethod.isAnnotationPresent(restAnnotationClass);

                if (isRest) {
                    response.setContentType("application/json;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(objectMapper.writeValueAsString(result));
                    }
                    return;
                }

                // CAS 2 : ViewUtil (pas de @Rest) → forward vers une JSP
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

                // CAS 3 : String (pas de @Rest) → texte brut
                if (result instanceof String text) {
                    response.setContentType("text/plain;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Resultat de la methode:\n");
                        out.println(text);
                    }
                    return;
                }

                // CAS 4 : Injection (pas de @Rest) → sauvegarde en session + affichage du contenu
                if (result instanceof Injection injection) {
                    saveSession(injection, request.getSession());
                    response.setContentType("text/plain;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Contenu de la session pour " + urlMethod + " :");
                        injection.getSession().forEach((key, value) -> out.println(key + " = " + value));
                    }
                    return;
                }

                throw new ServletException(
                        "Type de retour non supporté pour " + urlMethod + " : " + result.getClass().getName()
                                + " (ajoute @Rest sur la méthode pour renvoyer du JSON, ou renvoie un ViewUtil pour une vue)");

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

    /**
     * Détermine la valeur à passer pour le paramètre n°index de la méthode du controller :
     *  - ApplicationContext / HttpServletRequest / HttpServletResponse : injectés tels quels
     *  - type simple (String, int, Long, double, boolean, LocalDate, enum...) : lu dans la requête
     *    sous le NOM du paramètre Java (nécessite la compilation avec -parameters, activée
     *    par spring-boot-starter-parent)
     *  - FileUpload / FileUpload[] : fichier(s) du formulaire multipart (nom du champ = nom du paramètre)
     *  - autre type (ex: Product) : construit depuis les champs du formulaire via BindParam.bind
     */
    private Object populateParameter(Method controllerMethod, int index, HttpServletRequest request,
            HttpServletResponse response, UrlMethod urlMethod) throws ServletException {
        Class<?> paramType = controllerMethod.getParameterTypes()[index];
        String paramName = controllerMethod.getParameters()[index].getName();

        if (paramType.equals(ApplicationContext.class)) {
            return springContext;
        }
        if (paramType.equals(HttpServletRequest.class)) {
            return request;
        }
        if (paramType.equals(HttpServletResponse.class)) {
            return response;
        }

        try {
            if (paramType.equals(FileUpload.class)) {
                if (!FileUpload.isMultipart(request)) {
                    return null;
                }
                jakarta.servlet.http.Part part = request.getPart(paramName);
                return part == null ? null : FileUpload.convertToFileUpload(part);
            }
            if (paramType.equals(FileUpload[].class)) {
                if (!FileUpload.isMultipart(request)) {
                    return new FileUpload[0];
                }
                return FileUpload.fromAll(request, paramName);
            }
            if (isSimpleType(paramType)) {
                String value = request.getParameter(paramName);
                if (value == null || value.isBlank()) {
                    return BindParam.defaultValue(paramType);
                }
                return BindParam.convert(value, paramType);
            }

            if (paramType.equals(Injection.class)) {
                return loadSession(request.getSession());
            }
            return BindParam.bind(paramType, request);
        } catch (Exception e) {
            throw new ServletException(
                    "Erreur lors de la liaison du paramètre '" + paramName + "' pour " + urlMethod, e);
        }
    }

    /** Vrai si la session de l'utilisateur contient l'attribut "utilisateur connecté". */
    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        return httpSession != null && httpSession.getAttribute(userSessionKey) != null;
    }

    /**
     * Crée une Injection propre à la requête courante et la remplit avec les attributs
     * de la session de l'utilisateur. Une nouvelle instance à chaque requête : deux
     * utilisateurs qui appellent le serveur en même temps ne partagent jamais la même map.
     */
    private Injection loadSession(HttpSession httpSession) {
        Injection injection = new Injection();
        for (String name : Collections.list(httpSession.getAttributeNames())) {
            injection.injectSessionAttribute(name, httpSession.getAttribute(name));
        }
        return injection;
    }

    /** Recopie la map de l'Injection dans la HttpSession (ajouts, modifications et suppressions). */
    private void saveSession(Injection injection, HttpSession httpSession) {
        Map<String, Object> session = injection.getSession();
        if (session == null) {
            return;
        }
        for (String name : new ArrayList<>(Collections.list(httpSession.getAttributeNames()))) {
            if (!session.containsKey(name)) {
                httpSession.removeAttribute(name);
            }
        }
        session.forEach(httpSession::setAttribute);
    }

    private boolean isSimpleType(Class<?> t) {
        return t.isPrimitive() || t.equals(String.class) || t.equals(java.time.LocalDate.class)
                || t.isEnum() || Number.class.isAssignableFrom(t)
                || t.equals(Boolean.class) || t.equals(Character.class);
    }
}
