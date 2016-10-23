package jet.nsi.api;

import java.util.Collection;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.model.MetaDict;
import org.jooq.Meta;

public interface NsiMetaService {

    /**
     * Получить список коротких описаний справочников
     */
    Collection<NsiConfigDict> metaDictList(String requestId);

    /**
     * Получить полное описание справочника
     */
    NsiConfigDict metaDictGet(String requestId, String name);

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
