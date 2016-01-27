
package jet.isur.nsi.api.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Описание справочника.
 */
/**
 * @author balmaster
 *
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
     * атрибут уникально идентифицирующий запись среди неудалённых.
     */
    private String uniqueAttr;
    /**
     * Список наименований атрибутов кторые используются для текстового представления записи.
     */
    private List<String> captionAttrs;
    /**
     * Список наименований атрибутов кторые используются для короткого представления записи.
     */
    private List<String> refObjectAttrs;
    /**
     * Список наименований атрибутов которые используются в задачах загрузки
     * данных
     */
    private List<String> loadDataAttrs;
    /**
     * Список наименований атрибутов кторые используются для табличного
     * представления записи.
     */
    private List<String> tableObjectAttrs;
    /**
     * Список интерсепторов, которые должны выполняться при создании, сохранении или удалении записи
     */
    private List<String> interceptors;

    /**
     * Флаг определяет скрытый словарь или нет. В админке справочников отображаются только нескрытые словари.
     */
    private Boolean hidden;

    /**
     * Список наименований атрибутов кторые используются для мерджа данных, поступивших из внешней системы
     * 
     */
    private List<String> mergeExternalAttrs;
    
    /**
     * Именованные запросы
     */
    private Map<String, MetaSourceQuery> sourceQueries;

    /**
     * Принадлежности
     */
    private Map<String, MetaOwn> owns;

    /**
     * Показывает что сущность не самостоятельна и отображается на заданную.
     * Этот атрибут позволяет через view делать операции изменения данных.
     * При этом учитываются только те атрибуты кторые есть в @mainDict
     */
    private String mainDict;

    /**
     * Атрибут для представления версии
     */
    private String versionAttr;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения версии
     */
    private boolean autoVersion = true;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения отметки о удалении
     */
    private boolean autoDeleteMarkAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения идентификатора
     */
    private boolean autoIdAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения отметки о том что запись является группой
     */
    private boolean autoIsGroupAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения ссылки на родителя
     */
    private boolean autoParentAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения времени модификации
     */
    private boolean autoLastChangeAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения ид пользователя сделавшего последнии изменения
     */
    private boolean autoLastUserAttr = false;
    
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

    public List<String> getLoadDataAttrs() {
        return loadDataAttrs;
    }

    public void setLoadDataAttrs(List<String> loadDataAttrs) {
        this.loadDataAttrs = loadDataAttrs;
    }
    public List<String> getTableObjectAttrs() {
        return tableObjectAttrs;
    }
    public void setTableObjectAttrs(List<String> tableObjectAttrs) {
        this.tableObjectAttrs = tableObjectAttrs;
    }

    public List<String> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<String> interceptors) {
        this.interceptors = interceptors;
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
    public String getMainDict() {
        return mainDict;
    }
    public void setMainDict(String mainDict) {
        this.mainDict = mainDict;
    }

    public Map<String, MetaOwn> getOwns() {
        return owns;
    }

    public void setOwns(Map<String, MetaOwn> owns) {
        this.owns = owns;
    }
	public List<String> getMergeExternalAttrs() {
		return mergeExternalAttrs;
	}
	public void setMergeExternalAttrs(List<String> mergeExternalAttrs) {
		this.mergeExternalAttrs = mergeExternalAttrs;
	}
    public String getUniqueAttr() {
        return uniqueAttr;
    }
    public void setUniqueAttr(String uniqueAttr) {
        this.uniqueAttr = uniqueAttr;
    }
    public boolean isAutoVersion() {
        return autoVersion;
    }
    public void setAutoVersion(boolean autoVersion) {
        this.autoVersion = autoVersion;
    }
    public String getVersionAttr() {
        return versionAttr;
    }
    public void setVersionAttr(String versionAttr) {
        this.versionAttr = versionAttr;
    }
    public boolean isAutoDeleteMarkAttr() {
        return autoDeleteMarkAttr;
    }
    public void setAutoDeleteMarkAttr(boolean autoDeleteMarkAttr) {
        this.autoDeleteMarkAttr = autoDeleteMarkAttr;
    }
    public boolean isAutoIdAttr() {
        return autoIdAttr;
    }
    public void setAutoIdAttr(boolean autoIdAttr) {
        this.autoIdAttr = autoIdAttr;
    }
    public boolean isAutoIsGroupAttr() {
        return autoIsGroupAttr;
    }
    public void setAutoIsGroupAttr(boolean autoIsGroupAttr) {
        this.autoIsGroupAttr = autoIsGroupAttr;
    }
    public boolean isAutoParentAttr() {
        return autoParentAttr;
    }
    public void setAutoParentAttr(boolean autoParentAttr) {
        this.autoParentAttr = autoParentAttr;
    }
    public boolean isAutoLastChangeAttr() {
        return autoLastChangeAttr;
    }
    public void setAutoLastChangeAttr(boolean autoLastChangeAttr) {
        this.autoLastChangeAttr = autoLastChangeAttr;
    }
    public boolean isAutoLastUserAttr() {
        return autoLastUserAttr;
    }
    public void setAutoLastUserAttr(boolean autoLastUserAttr) {
        this.autoLastUserAttr = autoLastUserAttr;
    }
    
}