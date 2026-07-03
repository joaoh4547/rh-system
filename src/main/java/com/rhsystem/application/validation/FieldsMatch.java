package com.rhsystem.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint: the values of {@code first} and {@code second}
 * properties must be equal (e.g. password + confirmation).
 *
 * <p>The {@code message} is an i18n key, translated at the UI layer.
 */
@Documented
@Constraint(validatedBy = FieldsMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldsMatch.List.class)
public @interface FieldsMatch {

    String first();

    String second();

    String message() default "error.fields.mismatch";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        FieldsMatch[] value();
    }
}
