package jet.nsi.api.validator;

import com.google.common.io.Files;
import jet.afs.common.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ReservedWordsValidator implements ConstraintValidator<ReservedWordsConstraint, String> {
    @Value("${nsi.reservedWordsConfigPath:classpath:reservedWords.cfg}")
    private Resource reservedWordsConfigPath;
    private Set<String> reservedWords;

    @Override
    public void initialize(ReservedWordsConstraint constraintAnnotation) {
        try {
            List<String> lines = Files.readLines(reservedWordsConfigPath.getFile(), Charset.forName("utf-8"));
            reservedWords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            reservedWords.addAll(lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        ValidationUtil.setMessage(context, "reservedWordError." + value);
        return !reservedWords.contains(value);
    }

}
