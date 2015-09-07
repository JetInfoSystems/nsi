package jet.isur.nsi.api.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Описание справочника.
 */
public class MetaDict extends MetaDictRef {
    private static final long serialVersionUID = 1L;

    /**
     * Уникальное имя справочника.
     */
    private String name;

    /**
     * Заголовок справочника.
     */
    private String caption;
    /**
     * Имя таблицы для хранения данных.
     */
    private String table;
    /**
     * Список полей таблицы.
     */
    private Collection<MetaField> fields;
    /**
     * Список атрибутов справочника.
     */
    private Collection<MetaAttr> attrs;
    /**
     * атрибут кторый представлет ид.
     */
    private String idAttr;
    /**
     * атрибут который представляет ссылку на родителя в иерархии.
     */
    private String parentAttr;
    /**
     * атрибут который представляет является ли запись группой.
     */
    private String isGroupAttr;
    /**
     * атрибут который представляет ссылку на запись родительского справочника.
     */
    private String ownerAttr;
    /**
     * атрибут который представляет пометку о удалении.
     */
    private String deleteMarkAttr;
    /**
     * атрибут который представляет дату время последней модификации.
     */
    private String lastChangeAttr;
    /**
     * атрибут который представляет уникальный идентификатор пользователя последний раз менявшего запись.
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

    private Boolean hidden;

    private Map<String, MetaSourceQuery> sourceQueries;

    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public Collection<MetaField> getFields() {
        return fields;
    }
    public void setFields(Collection<MetaField> fields) {
        this.fields = fields;
    }
    public Collection<MetaAttr> getAttrs() {
        return attrs;
    }
    public void setAttrs(Collection<MetaAttr> attrs) {
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
    public String getIsGroupAttr() {
        return isGroupAttr;
    }
    public void setIsGroupAttr(String groupAttr) {
        this.isGroupAttr = groupAttr;
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
    public Boolean getHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
    public Map<String, MetaSourceQuery> getSourceQueries() {
        return sourceQueries;
    }
    public void setSourceQueries(Map<String, MetaSourceQuery> sourceQueries) {
        this.sourceQueries = sourceQueries;
    }

}
