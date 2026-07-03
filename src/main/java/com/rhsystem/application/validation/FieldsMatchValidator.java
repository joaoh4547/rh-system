package com.rhsystem.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Validator for {@link FieldsMatch}. Reads properties via record accessor
 * ({@code name()}) or JavaBean getter ({@code getName()}).
 */
public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {

    private String first;
    private String second;

    @Override
    public void initialize(FieldsMatch annotation) {
        this.first = annotation.first();
        this.second = annotation.second();
    }

    @Override
    public boolean isValid(Object bean, ConstraintValidatorContext context) {
        if (bean == null) {
            return true;
        }
        Object a = read(bean, first);
        Object b = read(bean, second);
        if (Objects.equals(a, b)) {
            return true;
        }
        // Attach the violation to the second field (e.g. the confirmation input)
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(second)
                .addConstraintViolation();
        return false;
    }

    private static Object read(Object bean, String property) {
        try {
            Method accessor = findAccessor(bean.getClass(), property);
            accessor.setAccessible(true);
            return accessor.invoke(bean);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "FieldsMatch: property '" + property + "' not readable on " + bean.getClass(), e);
        }
    }

    private static Method findAccessor(Class<?> type, String property) throws NoSuchMethodException {
        try {
            return type.getMethod(property); // record accessor
        } catch (NoSuchMethodException e) {
            String capitalized = Character.toUpperCase(property.charAt(0)) + property.substring(1);
            return type.getMethod("get" + capitalized); // JavaBean getter
        }
    }
}
