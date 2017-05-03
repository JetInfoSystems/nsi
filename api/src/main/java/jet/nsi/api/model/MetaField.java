package jet.nsi.api.model;

import jet.nsi.api.validator.SizeAndPrecisionConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Описание поля таблицы.
 */
@SizeAndPrecisionConstraint.List({
        @SizeAndPrecisionConstraint(dataType = MetaFieldType.DATE_TIME),
        @SizeAndPrecisionConstraint(dataType = MetaFieldType.CLOB)
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaField implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Уникальное имя поля в таблице.
     */
    @NotBlank
    private String name;
    /**
     * Тип поля: string | number | date-time | boolean.
     */
    @NotNull
    private MetaFieldType type = MetaFieldType.VARCHAR;

    /**
     * Размер.
     */
    private Integer size = 0;
    /**
     * Точность.
     */
    private Integer precision = 0;

    /**
     * Если данный map задан то поле является перечислением.
     */
    private Map<String, String> enumValues;

    /**
     * Значение по умолчанию.
     */
    private String defaultValue;

    /**
     * Определяет возможность выполнения полнотекстового поиска по данному атрибуту
     * Поддерживается только для строковых типов.
     */
    private boolean enableFts = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetaFieldType getType() {
        return type;
    }

    public void setType(MetaFieldType type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Map<String, String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(Map<String, String> enumValues) {
        this.enumValues = enumValues;
    }

    public boolean isEnableFts() {
        return enableFts;
    }

    public void setEnableFts(boolean enableFts) {
        this.enableFts = enableFts;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


}
