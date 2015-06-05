package jet.isur.nsi.api.model;

import java.io.Serializable;

/**
 * Описание поля таблицы.
 */
public class MetaField implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Уникальное имя поля в таблице.
     */
    private String name;
    /**
     * Тип поля: string | number | date-time | boolean.
     */
    private MetaFieldType type = MetaFieldType.STRING;

    /**
     * Размер.
     */
    private Integer size = 0;
    /**
     * Точность.
     */
    private Integer precision = 0;
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


}
