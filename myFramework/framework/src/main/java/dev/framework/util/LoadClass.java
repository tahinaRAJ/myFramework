package dev.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class LoadClass {

    // --- CONFIG ---

    private static Properties loadConfigProperties() {
        Properties prop = new Properties();
        try (InputStream input = LoadClass.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("[ERREUR] Impossible de trouver le fichier config.properties.");
            }
            prop.load(input);
            return prop;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture de config.properties", e);
        }
    }

    // --- UTILITAIRES ---

    public static boolean hasAnnotation(Class<?> clazzScanned, String monAnnotation) {
        try {
            Class<?> clazz = Class.forName(monAnnotation);
            Class<? extends Annotation> annotationClass = clazz.asSubclass(Annotation.class);
            Target target = annotationClass.getAnnotation(Target.class);
            if (target != null && Arrays.asList(target.value()).contains(ElementType.METHOD)) {
                return Arrays.stream(clazzScanned.getDeclaredMethods())
                        .anyMatch(m -> m.isAnnotationPresent(annotationClass));
            }
            return clazzScanned.isAnnotationPresent(annotationClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Annotation non trouvée : " + monAnnotation, e);
        }
    }



    // public static List<Class<?>> getControllers(String basePackage) throws Exception {
    //     List<Class<?>> result = new ArrayList<>();
    //     try (ScanResult scanResult = new ClassGraph()
    //             .enableAllInfo()
    //             .acceptPackages(basePackage)
    //             .scan()) {
    //         for (ClassInfo classInfo : scanResult.getAllClasses()) {
    //             Class<?> clazz = Class.forName(classInfo.getName());
    //             if (hasAnnotation(clazz, "dev.framework.annotation.Controller")) {
    //                 result.add(clazz);
    //             }
    //         }
    //     }
    //     return result;
    // }

    // public static List<Class<?>> getControllers() throws Exception {
    //     List<Class<?>> result = new ArrayList<>();
    //     try (ScanResult scanResult = new ClassGraph()
    //             .enableAllInfo()
    //             .scan()) {
    //         for (ClassInfo classInfo : scanResult.getAllClasses()) {
    //             Class<?> clazz = Class.forName(classInfo.getName());
    //             if (hasAnnotation(clazz, "dev.framework.annotation.Controller")) {
    //                 result.add(clazz);
    //             }
    //         }
    //     }
    //     return result;
    // }

    // --- SCAN AVEC config.properties

    public static void loadUrlMappingsWithMethod(String packageName, Map<UrlMethod, Mapping> routes)
            throws IllegalStateException {
        Properties prop = loadConfigProperties();
        Class<? extends Annotation> controllerAnnotationClass;
        Class<? extends Annotation> urlMappingAnnotationClass;

        try {
            String controllerClassName = prop.getProperty("annotation.controller");
            String urlMappingClassName = prop.getProperty("annotation.mapping");

            if (controllerClassName == null || controllerClassName.isBlank())
                throw new RuntimeException("[ERREUR] 'annotation.controller' manquant dans config.properties.");
            if (urlMappingClassName == null || urlMappingClassName.isBlank())
                throw new RuntimeException("[ERREUR] 'annotation.mapping' manquant dans config.properties.");

            controllerAnnotationClass = Class.forName(controllerClassName).asSubclass(Annotation.class);
            urlMappingAnnotationClass = Class.forName(urlMappingClassName).asSubclass(Annotation.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Une des annotations configurées n'existe pas", e);
        }

        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());
                    if (!clazz.isAnnotationPresent(controllerAnnotationClass)) continue;

                    for (Method method : clazz.getDeclaredMethods()) {
                        Annotation urlMapping = method.getAnnotation(urlMappingAnnotationClass);
                        if (urlMapping != null) {
                            String url        = (String) urlMappingAnnotationClass.getMethod("value").invoke(urlMapping);
                            String methodType = (String) urlMappingAnnotationClass.getMethod("method").invoke(urlMapping);
                            UrlMethod urlMethod = new UrlMethod(url, methodType);

                            if (routes.containsKey(urlMethod)) {
                                Mapping existing = routes.get(urlMethod);
                                throw new IllegalStateException("""
                                        [ERREUR] Conflit de routes détecté !
                                        La route [%s %s] est déjà associée à : %s.%s()
                                        Impossible de la réassigner à : %s.%s()
                                        """.formatted(methodType, url,
                                        existing.getControllerClass().getName(), existing.getMethod().getName(),
                                        clazz.getName(), method.getName()));
                            }

                            routes.put(urlMethod, new Mapping(clazz, method));
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException e) {
                    throw new RuntimeException("Erreur lors du chargement de : " + classInfo.getName(), e);
                }
            }
        }
    }

    public static Map<UrlMethod, Mapping> loadUrlMappingsWithMethod() throws IllegalStateException {
        Map<UrlMethod, Mapping> routes = new HashMap<>();
        Properties prop = loadConfigProperties();
        Class<? extends Annotation> controllerAnnotationClass;
        Class<? extends Annotation> urlMappingAnnotationClass;

        try {
            String controllerClassName = prop.getProperty("annotation.controller");
            String urlMappingClassName = prop.getProperty("annotation.mapping");

            controllerAnnotationClass = Class.forName(controllerClassName).asSubclass(Annotation.class);
            urlMappingAnnotationClass = Class.forName(urlMappingClassName).asSubclass(Annotation.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Une des annotations configurées n'existe pas", e);
        }

        try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());
                    if (!clazz.isAnnotationPresent(controllerAnnotationClass)) continue;

                    for (Method method : clazz.getDeclaredMethods()) {
                        Annotation urlMapping = method.getAnnotation(urlMappingAnnotationClass);
                        if (urlMapping != null) {
                            String url        = (String) urlMappingAnnotationClass.getMethod("value").invoke(urlMapping);
                            String methodType = (String) urlMappingAnnotationClass.getMethod("method").invoke(urlMapping);
                            UrlMethod urlMethod = new UrlMethod(url, methodType);

                            if (routes.containsKey(urlMethod)) {
                                Mapping existing = routes.get(urlMethod);
                                throw new IllegalStateException("""
                                        [ERREUR] Conflit de routes détecté !
                                        La route [%s %s] est déjà associée à : %s.%s()
                                        Impossible de la réassigner à : %s.%s()
                                        """.formatted(methodType, url,
                                        existing.getControllerClass().getName(), existing.getMethod().getName(),
                                        clazz.getName(), method.getName()));
                            }

                            routes.put(urlMethod, new Mapping(clazz, method));
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException e) {
                    throw new RuntimeException("Erreur lors du chargement de : " + classInfo.getName(), e);
                }
            }
        }
        return routes;
    }

    //AUTRES UTILITAIRES 

    public static List<String> loadClassWithMyAnnotation(String packageName, String monAnnotation) {
        List<String> liste = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(monAnnotation)) {
                liste.add(classInfo.getName());
            }
        }
        return liste;
    }

    public static List<String> loadClassWithMyMethodeAnnotation(String packageName, String monAnnotation) {
        List<String> liste = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithMethodAnnotation(monAnnotation)) {
                liste.add(classInfo.getName());
            }
        }
        return liste;
    }

    public static List<String> loadAllClasses() {
        List<String> liste = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                liste.add(classInfo.getName());
            }
        }
        return liste;
    }

    public static boolean isARouteInsideMappingWithMethod(UrlMethod urlMethod, Map<UrlMethod, Mapping> routes) {
        return routes.containsKey(urlMethod);
    }
}
