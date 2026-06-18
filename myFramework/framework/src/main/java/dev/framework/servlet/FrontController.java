    package dev.framework.servlet;

    import java.io.IOException;
    import java.lang.reflect.Method;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import dev.framework.annotation.Controller;
    import dev.framework.util.LoadClass;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    public class FrontController extends HttpServlet {
        private final Map<String, Method>  routes    = new HashMap<>();
        private final Map<String, Object>  instances = new HashMap<>();

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
                    Controller classAnnotation = clazz.getDeclaredAnnotation(Controller.class);
                    String basePath = (classAnnotation != null) ? classAnnotation.value() : "";
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Controller.class)) {
                            Controller methodAnnotation = method.getDeclaredAnnotation(Controller.class);
                            String methodPath = (methodAnnotation != null) ? methodAnnotation.value() : "";
                            String fullPath = basePath + methodPath;

                            routes.put(fullPath, method);
                            instances.put(fullPath, instance);

                            System.out.println("[Framework] Route enregistrée : " + fullPath
                                    + " → " + clazz.getSimpleName() + "." + method.getName() + "()");
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
            processRequest(request, response);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            processRequest(request, response);
        }

        public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
            String url = request.getRequestURL().toString();
            System.out.println("[Framework] URL: " + url);
            response.getWriter().println("URL recue: " + url);
        }

    }
