package org.policedog.registry.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.policedog.registry.domain.Status;
import org.policedog.registry.validator.annotation.ValidStatus;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidStatusValidator implements ConstraintValidator<ValidStatus, Status> {

    private Set<Status> allowedStatus;
    private String allowedStatusString;

    @Override
    public void initialize(ValidStatus constraintAnnotation) {
        Status[] values = constraintAnnotation.value();
        this.allowedStatus = Set.of(values);
        this.allowedStatusString = Arrays.stream(values)
                .map(Status::name)
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean isValid(Status status, ConstraintValidatorContext constraintValidatorContext) {
        // Null values are handled by @NotNull annotation
        if (status == null) {
            return true;
        }

        if (!allowedStatus.contains(status)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(String.format(
                            "Invalid status '%s'. Allowed values are: %s.", status.name(), allowedStatusString
                    ))
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
