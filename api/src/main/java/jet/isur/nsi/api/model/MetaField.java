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
    private String type;
    public static final String TYPE_STRING = "string";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_DATE_TIME = "date-time";
    public static final String TYPE_BOOLEAN = "boolean";
    /**
     * Размер.
     */
    private Integer size;
    /**
     * Точность.
     */
    private Integer precision;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
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
