package dev.framework.util;

import java.lang.reflect.Method;
import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Liaison requête HTTP -> objets Java.
 *
 * - bind(...)    : construit un objet (ex: Product) en appelant ses setters, avec les
 *                  paramètres de la requête portant le même nom que la propriété
 *                  (setName -> "name", setPrice -> "price"...).
 * - convert(...) : convertit une String vers le type voulu (int, long, double, boolean,
 *                  char, LocalDate, enum, ...). Réutilisé aussi pour les paramètres simples
 *                  des méthodes de controller (ex: delete(Long id)).
 */
public class BindParam {

    public static Object bind(Class<?> clazz, HttpServletRequest request) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();

        for (Method method : clazz.getMethods()) {
            if (!method.getName().startsWith("set") || method.getParameterCount() != 1
                    || method.getName().length() <= 3) {
                continue;
            }

            String propertyName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            String propertyValue = request.getParameter(propertyName);
            if (propertyValue == null || propertyValue.isBlank()) {
                continue; // champ absent du formulaire -> valeur par défaut conservée
            }

            method.invoke(obj, convert(propertyValue, method.getParameterTypes()[0]));
        }

        return obj;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object convert(String v, Class<?> t) {
        if (t == String.class)
            return v;
        if (t == int.class || t == Integer.class)
            return Integer.valueOf(v);
        if (t == byte.class || t == Byte.class)
            return Byte.valueOf(v);
        if (t == short.class || t == Short.class)
            return Short.valueOf(v);
        if (t == long.class || t == Long.class)
            return Long.valueOf(v);
        if (t == float.class || t == Float.class)
            return Float.valueOf(v);
        if (t == double.class || t == Double.class)
            return Double.valueOf(v);
        if (t == boolean.class || t == Boolean.class)
            return Boolean.valueOf(v);
        if (t == char.class || t == Character.class) {
            if (v.length() != 1)
                throw new IllegalArgumentException("Un seul caractère attendu : " + v);
            return v.charAt(0);
        }
        if (t == LocalDate.class)
            return LocalDate.parse(v);
        if (t.isEnum())
            return Enum.valueOf((Class<Enum>) t, v);
        throw new IllegalArgumentException("Type non supporté : " + t.getName());
    }

    /** Valeur par défaut d'un type primitif quand le paramètre est absent (0, false, ...). */
    public static Object defaultValue(Class<?> t) {
        if (t == boolean.class) return false;
        if (t == char.class) return '\0';
        if (t == byte.class) return (byte) 0;
        if (t == short.class) return (short) 0;
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == float.class) return 0F;
        if (t == double.class) return 0D;
        return null;
    }
}
