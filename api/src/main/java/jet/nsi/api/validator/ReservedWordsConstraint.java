package jet.nsi.api.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Constraint(validatedBy = ReservedWordsValidator.class)
@Retention(RUNTIME)
public @interface ReservedWordsConstraint {

    String message() default "ReservedWordsConstraintMessage";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
