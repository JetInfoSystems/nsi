package jet.nsi.api.validator;

import jet.nsi.api.model.MetaFieldType;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = DataTypeValidator.class)
@Documented
public @interface SizeAndPrecisionConstraint {
    MetaFieldType dataType();

    String message() default "{SizeAndPrecisionConstraint.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({TYPE, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        SizeAndPrecisionConstraint[] value();
    }
}
