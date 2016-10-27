package jet.nsi.api;

import java.util.Collection;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.model.MetaDict;

public interface NsiMetaEditorService {

    /**
     * Получить список описаний справочников
     */
    Collection<MetaDict> metaDictList(String requestId);

    /**
     * Получить полное описание справочника
     */
    MetaDict metaDictGet(String requestId, String name);

    /**
     * Сохранить описание справочника на диск
     */
    MetaDict metaDictSet(String requestId, MetaDict metaDict);

    /**
     * Создать новое описание справочника
     */
    MetaDict metaDictCreate(String requestId, String dictName);

    /**
     * Получить конфигурацию метаданных
     */
    NsiConfig getConfig();
}
