package com.rhsystem.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates a Brazilian CPF by check digits (accepts masked or digits-only input).
 * Blank values are considered valid — combine with {@code @NotBlank} when required.
 *
 * <p>The {@code message} is an i18n key, translated at the UI layer.
 */
@Documented
@Constraint(validatedBy = CpfConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CPF {

    String message() default "error.cpf.invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
