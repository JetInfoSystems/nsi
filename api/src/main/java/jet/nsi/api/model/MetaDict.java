
package jet.nsi.api.model;

import javax.validation.Valid;
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
    protected String name;

    /**
     * Заголовок справочника.
     */
    protected String caption;
    /**
     * Имя таблицы для хранения данных.
     */
    protected String table;
    /**
     * Название базы данных
     */
    protected String databaseName;
    /**
     * Список полей таблицы.
     */
    @Valid
    protected Collection<MetaField> fields;
    /**
     * Список атрибутов справочника.
     */
    @Valid
    protected Collection<MetaAttr> attrs;
    /**
     * атрибут кторый представляет ид.
     */
    protected String idAttr;
    /**
     * атрибут который представляет ссылку на родителя в иерархии.
     */
    protected String parentAttr;
    /**
     * атрибут который представляет является ли запись группой.
     */
    protected String isGroupAttr;
    /**
     * атрибут который представляет ссылку на запись родительского справочника.
     */
    protected String ownerAttr;
    /**
     * атрибут который представляет пометку о удалении.
     */
    protected String deleteMarkAttr;
    /**
     * атрибут который представляет дату время последней модификации.
     */
    protected String lastChangeAttr;
    /**
     * атрибут который представляет уникальный идентификатор пользователя последний раз менявшего запись.
     */
    protected String lastUserAttr;
    /**
     * атрибут уникально идентифицирующий запись среди неудалённых.
     */
    protected List<String> uniqueAttr;
    /**
     * Список наименований атрибутов кторые используются для текстового представления записи.
     */
    protected List<String> captionAttrs;
    /**
     * Список наименований атрибутов кторые используются для короткого представления записи.
     */
    protected List<String> refObjectAttrs;
    /**
     * Список наименований атрибутов которые используются в задачах загрузки
     * данных
     */
    protected List<String> loadDataAttrs;
    /**
     * Список наименований атрибутов кторые используются для табличного
     * представления записи.
     */
    protected List<String> tableObjectAttrs;
    /**
     * Список интерсепторов, которые должны выполняться при создании, сохранении или удалении записи
     */
    protected List<String> interceptors;

    /**
     * Список меток для фильтрации метаданных
     * */
    protected List<String> labels;
    /**
     * Флаг определяет скрытый словарь или нет. В админке справочников отображаются только нескрытые словари.
     */
    protected Boolean hidden;

    /**
     * Список наименований атрибутов кторые используются для мерджа данных, поступивших из внешней системы
     * 
     */
    protected List<String> mergeExternalAttrs;
    
    /**
     * Именованные запросы
     */
    protected Map<String, MetaSourceQuery> sourceQueries;

    /**
     * Принадлежности
     */
    protected Map<String, MetaOwn> owns;

    /**
     * Показывает что сущность не самостоятельна и отображается на заданную.
     * Этот атрибут позволяет через view делать операции изменения данных.
     * При этом учитываются только те атрибуты кторые есть в @mainDict
     */
    protected String mainDict;

    /**
     * Атрибут для представления версии
     */
    protected String versionAttr;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения версии
     */
    protected boolean autoVersion = true;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения отметки о удалении
     */
    protected boolean autoDeleteMarkAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения идентификатора
     */
    protected boolean autoIdAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения отметки о том что запись является группой
     */
    protected boolean autoIsGroupAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения ссылки на родителя
     */
    protected boolean autoParentAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения времени модификации
     */
    protected boolean autoLastChangeAttr = false;
    
    /**
     * Автоматическое генерирование поля и атрибута для хранения ид пользователя сделавшего последнии изменения
     */
    protected boolean autoLastUserAttr = false;
    
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

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
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
    public List<String> getUniqueAttr() {
        return uniqueAttr;
    }
    public void setUniqueAttr(List<String> uniqueAttr) {
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

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

}