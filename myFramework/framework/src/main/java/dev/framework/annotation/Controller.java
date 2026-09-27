package dev.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Marque une classe comme controller du framework.
 * Meta-annotée par @Component : Spring va donc automatiquement créer
 * un bean pour chaque classe annotée (via component-scan), ce qui permet
 * d'injecter des @Autowired (Service, Repository...) dans les controllers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Component
public @interface Controller {
    String value() default "";
}
