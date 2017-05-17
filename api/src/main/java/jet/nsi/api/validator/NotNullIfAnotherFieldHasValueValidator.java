package jet.nsi.api.validator;


import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullIfAnotherFieldHasValueValidator implements ConstraintValidator<NotNullIfAnotherFieldHasValue, Object> {
    private static final Logger log = LoggerFactory.getLogger(NotNullIfAnotherFieldHasValueValidator.class);
    private String fieldName;
    private String expectedFieldValue;
    private String dependFieldName;

    @Override
    public void initialize(final NotNullIfAnotherFieldHasValue annotation) {
        fieldName = annotation.fieldName();
        expectedFieldValue = annotation.fieldValue();
        dependFieldName = annotation.dependFieldName();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext ctx) {

        if (value == null) {
            return true;
        }

        try {
            final String fieldValue = BeanUtils.getProperty(value, fieldName);
            final String dependFieldValue = BeanUtils.getProperty(value, dependFieldName);

            if (expectedFieldValue.equals(fieldValue) && dependFieldValue == null) {
                setMessage(ctx, "notNullConditionally." + dependFieldName + "." + fieldName + "." + expectedFieldValue);
                return false;
            }

        } catch (final Exception ex) {
            log.error("isValid->error; {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
        return true;
    }

    private void setMessage(ConstraintValidatorContext context, String message) { //todo в UTIL?
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

}