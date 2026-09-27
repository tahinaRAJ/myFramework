package dev.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A poser sur une méthode de controller pour indiquer que sa réponse doit être
 * sérialisée en JSON (via Jackson) plutôt que traitée comme une vue (ViewUtil -> JSP)
 * ou renvoyée comme texte brut.
 *
 * Exemple :
 *   @UrlMapping(value = "/api/products", method = "GET")
 *   @Rest
 *   public List<Product> apiList() {
 *       return productService.findAll(); // sérialisé en JSON
 *   }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Rest {
    String value() default "";
}
