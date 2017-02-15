package jet.nsi.api.validator;


import jet.nsi.api.model.MetaField;
import jet.nsi.api.model.MetaFieldType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DataTypeValidator implements ConstraintValidator<SizeAndPrecisionConstraint, MetaField> {
    private MetaFieldType dataType;

    @Override
    public void initialize(SizeAndPrecisionConstraint constraintAnnotation) {
        dataType = constraintAnnotation.dataType();
    }

    @Override
    public boolean isValid(MetaField value, ConstraintValidatorContext context) {
        if (value.getType() == null) {
            return false;
        }
        if (dataType != value.getType()) {
            return true;
        }

        switch (value.getType()) {
            case BOOLEAN: {
                return value.getSize() == 1 && isNullOrZero(value.getPrecision());
            }
            case DATE_TIME:
            case CLOB: {
                return isNullOrZero(value.getPrecision()) && isNullOrZero(value.getSize());
            }
            default: {
                throw new IllegalArgumentException("Unsupported dataType " + value.getType());
            }
        }

    }

    private boolean isNullOrZero(Integer value) {
        return value == null || value == 0;
    }
}
