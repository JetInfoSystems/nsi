package jet.isur.nsi.api.model;

import java.io.Serializable;

/**
 * Краткое описание справочника.
 */
public class MetaDictRef implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * Уникальное наименование справочника.
     */
    private String name;
    /**
     * Заголовок справочника для интерфейса.
     */
    private String caption;
    /**
     * Наименование справочника владельца.
     */
    private String ownerDict;
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
    public String getOwnerDict() {
        return ownerDict;
    }
    public void setOwnerDict(String ownerDict) {
        this.ownerDict = ownerDict;
    }
}
