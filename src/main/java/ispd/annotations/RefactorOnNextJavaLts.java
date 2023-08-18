package ispd.annotations;

import java.lang.annotation.*;

/**
 * Indicates that a method or constructor should be refactored once the project is upgraded to use
 * the next Java LTS.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface RefactorOnNextJavaLts {

    /**
     * @return The reason for the potential refactoring, such as {@code "Use Pattern Matching."}
     */
    String reason () default "";
}
