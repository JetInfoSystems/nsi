package jet.nsi.api.validator;


import jet.nsi.api.model.BoolExp;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static jet.nsi.api.model.OperationType.*;

public class FilterValueValidator implements ConstraintValidator<FilterValueConstraint, BoolExp> {
    private static final Set<String> fieldFunctions = new HashSet<>(Arrays.asList(EQUALS, NOT_EQUALS, GT, GE, LT, LE, IN, LIKE, NOTNULL, CONTAINS));

    @Override
    public void initialize(FilterValueConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(BoolExp value, ConstraintValidatorContext context) {
        if (fieldFunctions.contains(value.getFunc()) &&
                (value.getValue() == null || value.getValue().isEmpty())) {

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("FilterHasntValues." + value.getKey()).addConstraintViolation();

            return false;
        }
        return true;
    }
}
