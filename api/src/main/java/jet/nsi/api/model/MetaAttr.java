package jet.nsi.api.model;

import jet.nsi.api.validator.NotNullIfAnotherFieldHasValue;

import java.io.Serializable;
import java.util.List;

/**
 * Описание атрибута справочника.
 */
@NotNullIfAnotherFieldHasValue(fieldName = "type", fieldValue = "REF", dependFieldName = "refDict")
public class MetaAttr implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * Тип атрибута значение или ссылка: value | ref.
     */
    private MetaAttrType type;
    /**
     * Тип значения, используется в тех случаях когда набор полей описывает один атрибут, например адрес.
     */
    private String valueType;
    /**
     * Уникальное имя атрибута в справочнике.
     */
    private String name;
    /**
     * Заголовок атрибута
     */
    private String caption;
    /**
     * Список наименований.
     */
    private List<String> fields;
    /**
     * Наименование словаря на который ссылается атрибут.
     */
    private String refDict;
    /**
     * Атрибут является скрытым.
     */
    private Boolean hidden;
    /**
     * Если данный атрибут установлен в true то справочник не редактируется во frontend
     */
    private Boolean readonly;

	/**
	 * Атрибут можно задать при создании но в дальнейшем менять его нельзя
	 */
	private Boolean createOnly;

    /**
     * Атрибут является требуемым.
     * Если атрибут требуемый, и является ссылкой то для него будут формироваться join условие
     * Для нетребуемого атрибута будетформировать left join уcловие
     */
    private boolean required = false;

    /**
     * Атрибут определяет, должно ли значение полей атрибута использоваться для формирования текстового представления.
     * Фронтенд может использовать такие скрытые атрибуты, например для раскрашивания записей.
     */
    private boolean refAttrHidden = false;

    /**
     * Атрибут показывает является ли данный атрибут сохраняемым, используется чтобы сказать фронту какие поля
     * не нужно сохранять
     */
    private boolean persist = true;

    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean isRequired) {
        this.required = isRequired;
    }
    public MetaAttrType getType() {
        return type;
    }
    public void setType(MetaAttrType type) {
        this.type = type;
    }
    public String getValueType() {
        return valueType;
    }
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }
    public List<String> getFields() {
        return fields;
    }
    public void setFields(List<String> fields) {
        this.fields = fields;
    }
    public String getRefDict() {
        return refDict;
    }
    public void setRefDict(String refDict) {
        this.refDict = refDict;
    }
    public Boolean getHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getReadonly() {
        return readonly;
    }
    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

	public Boolean getCreateOnly() {
		return createOnly;
	}

	public void setCreateOnly(Boolean createOnly) {
		this.createOnly = createOnly;
	}

    public boolean isRefAttrHidden() {
        return refAttrHidden;
    }
    public void setRefAttrHidden(boolean refAttrHidden) {
        this.refAttrHidden = refAttrHidden;
    }
    public boolean isPersist() {
        return persist;
    }
    public void setPersist(boolean persist) {
        this.persist = persist;
    }
}
