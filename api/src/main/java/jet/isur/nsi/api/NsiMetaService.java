package jet.isur.nsi.api;

import java.util.Collection;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;

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
     * Получить конфигурацию метаданных
     */
    NsiConfig getConfig();
}
