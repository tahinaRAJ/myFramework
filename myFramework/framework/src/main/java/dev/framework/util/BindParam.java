package dev.framework.util;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Version simple : construit un objet (ex: Product) depuis les paramètres de la requête.
 * Chaque setter (setName, setPrice...) est rempli avec le paramètre du même nom ("name", "price").
 * Types gérés : String, int, long, double, boolean.
 */
public class BindParam {

    public static Object bind(Class<?> clazz, HttpServletRequest request) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();

        for (Method method : clazz.getMethods()) {
            if (!method.getName().startsWith("set") || method.getParameterCount() != 1
                    || method.getName().length() <= 3) {
                continue;
            }
            String name = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            String value = request.getParameter(name);
            if (value == null || value.isBlank()) {
                continue;
            }
            method.invoke(obj, convert(value, method.getParameterTypes()[0]));
        }
        return obj;
    }

    private static Object convert(String v, Class<?> t) {
        if (t == String.class) return v;
        if (t == int.class || t == Integer.class) return Integer.valueOf(v);
        if (t == long.class || t == Long.class) return Long.valueOf(v);
        if (t == double.class || t == Double.class) return Double.valueOf(v);
        if (t == boolean.class || t == Boolean.class) return Boolean.valueOf(v);
        throw new IllegalArgumentException("Type non supporté : " + t.getName());
    }
}
