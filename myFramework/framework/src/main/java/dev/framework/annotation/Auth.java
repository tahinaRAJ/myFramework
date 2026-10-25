package dev.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A poser sur une méthode de controller pour la réserver aux utilisateurs connectés.
 *
 * Avant d'appeler la méthode, le FrontController vérifie que la HttpSession contient
 * l'attribut dont le nom est défini dans le web.xml (context-param "auth.user-session-key").
 * Si ce n'est pas le cas, il répond 401 (Unauthorized) sans exécuter la méthode.
 *
 * Si des rôles sont précisés, le rôle de l'utilisateur (attribut de session défini par
 * le context-param "auth.role-session-key") doit faire partie de la liste, sinon 403 (Forbidden).
 *
 * Exemples :
 *   @Auth                                  -> il suffit d'être connecté
 *   @Auth(roles = "admin")                 -> connecté ET rôle "admin"
 *   @Auth(roles = {"admin", "manager"})    -> connecté ET rôle "admin" ou "manager"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Auth {
    String[] roles() default {};
}
