package jet.isur.nsi.api.model;

import java.util.List;

/**
 * Описание справочника.
 */
public class MetaDict extends MetaDictRef {
    private static final long serialVersionUID = 1L;


    /**
     * Имя таблицы для хранения данных.
     */
    private String table;
    /**
     * Список полей таблицы.
     */
    private List<MetaField> fields;
    /**
     * Список атрибутов справочника.
     */
    private List<MetaAttr> attrs;
    /**
     * Наименование атрибута кторый представлет ид.
     */
    private String idAttr;
    /**
     * Наименование атрибута который представляет ссылку на родителя в иерархии.
     */
    private String parentAttr;
    /**
     * Наименование атрибута который представляет является ли запись группой.
     */
    private String groupAttr;
    /**
     * Наименование атрибута который представляет ссылку на запись родительского справочника.
     */
    private String ownerAttr;
    /**
     * Наименование атрибута который представляет пометку о удалении.
     */
    private String deleteMarkAttr;
    /**
     * Наименование атрибута который представляет дату время последней модификации.
     */
    private String lastChangeAttr;
    /**
     * Наименование атрибута который представляет уникальный идентификатор пользователя последний раз менявшего запись.
     */
    private String lastUserAttr;
    /**
     * Список наименований атрибутов кторые используются для текстового представления записи.
     */
    private List<String> captionAttrs;
    /**
     * Список наименований атрибутов кторые используются для короткого представления записи.
     */
    private List<String> refObjectAttrs;
    /**
     * Список наименований атрибутов кторые используются для табличного представления записи.
     */
    private List<String> tableObjectAttrs;
    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public List<MetaField> getFields() {
        return fields;
    }
    public void setFields(List<MetaField> fields) {
        this.fields = fields;
    }
    public List<MetaAttr> getAttrs() {
        return attrs;
    }
    public void setAttrs(List<MetaAttr> attrs) {
        this.attrs = attrs;
    }
    public String getIdAttr() {
        return idAttr;
    }
    public void setIdAttr(String idAttr) {
        this.idAttr = idAttr;
    }
    public String getParentAttr() {
        return parentAttr;
    }
    public void setParentAttr(String parentAttr) {
        this.parentAttr = parentAttr;
    }
    public String getGroupAttr() {
        return groupAttr;
    }
    public void setGroupAttr(String groupAttr) {
        this.groupAttr = groupAttr;
    }
    public String getOwnerAttr() {
        return ownerAttr;
    }
    public void setOwnerAttr(String ownerAttr) {
        this.ownerAttr = ownerAttr;
    }
    public String getDeleteMarkAttr() {
        return deleteMarkAttr;
    }
    public void setDeleteMarkAttr(String deleteMarkAttr) {
        this.deleteMarkAttr = deleteMarkAttr;
    }
    public String getLastChangeAttr() {
        return lastChangeAttr;
    }
    public void setLastChangeAttr(String lastChangeAttr) {
        this.lastChangeAttr = lastChangeAttr;
    }
    public String getLastUserAttr() {
        return lastUserAttr;
    }
    public void setLastUserAttr(String lastUserAttr) {
        this.lastUserAttr = lastUserAttr;
    }
    public List<String> getCaptionAttrs() {
        return captionAttrs;
    }
    public void setCaptionAttrs(List<String> captionAttrs) {
        this.captionAttrs = captionAttrs;
    }
    public List<String> getRefObjectAttrs() {
        return refObjectAttrs;
    }
    public void setRefObjectAttrs(List<String> refObjectAttrs) {
        this.refObjectAttrs = refObjectAttrs;
    }
    public List<String> getTableObjectAttrs() {
        return tableObjectAttrs;
    }
    public void setTableObjectAttrs(List<String> tableObjectAttrs) {
        this.tableObjectAttrs = tableObjectAttrs;
    }

}
