package jet.nsi.api;

import java.util.Collection;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.model.MetaDict;
import org.jooq.Meta;

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
    void metaDictSet(MetaDict metaDict);

    /**
     * Создать новое описание справочника
     */
    MetaDict createMetaDict(String dictName);

    /**
     * Получить конфигурацию метаданных
     */
    NsiConfig getConfig();
}
