package org.policedog.registry.validator.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.policedog.registry.domain.Status;
import org.policedog.registry.validator.ValidStatusValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidStatusValidator.class)
@Documented
public @interface ValidStatus {

    Status[] value();

    String message() default "Invalid status. Allowed values are {allowed}.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
