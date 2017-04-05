package jet.nsi.api;

import java.util.Collection;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.model.MetaDict;

public interface NsiMetaEditorService {

    /**
     * Получить список описаний справочников
     * @param requestId - идентификатор запроса
     * @return коллекцию метаданных справочников
     */
    Collection<MetaDict> metaDictList(String requestId);

    /**
     * Получить полное описание справочника
     * @param requestId - идентификатор запроса
     * @param name - имя справочника
     * @return метаданные справочника
     */
    MetaDict metaDictGet(String requestId, String name);

    /**
     * Сохранить описание справочника
     * @param requestId - идентификатор запроса
     * @param metaDict - метаданные справочника
     * @return метаданные сохраненного справочника
     */
    MetaDict metaDictSet(String requestId, MetaDict metaDict);

    /**
     * Сохранить описание справочника
     * @param requestId - идентификатор запроса
     * @param metaDict - метаданные справочника
     * @param relativePath - относительный путь для сохранения файла
     * @return метаданные сохраненного справочника
     * */
    MetaDict metaDictSet(String requestId, MetaDict metaDict, String relativePath);

    /**
     * Создать новое описание справочника (оно не будет сохранено на диск! для сохранения нужно использовать set)
     * @param requestId - идентификатор запроса
     * @param metaDict - метаданные справочника
     * @return метаданные справочника (основная структура и предопределенные атрибуты и поля)
     */
    MetaDict metaDictCreate(String requestId, MetaDict metaDict);

    /**
     * Получить конфигурацию метаданных
     * @return конфигурацию Nsi метаданных
     */
    NsiConfig getConfig();
}
