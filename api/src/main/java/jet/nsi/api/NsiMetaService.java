package jet.nsi.api;

import java.util.Collection;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;

public interface NsiMetaService {

    /**
     * Получить список описаний справочников
     */
    Collection<NsiConfigDict> metaDictList(String requestId);
    
    /**
     * Получить список описаний справочников, отфильтрованных по меткам
     * В результат будет добавлен справочник, у которого присуствует хотя бы одна метка
     */
    Collection<NsiConfigDict> metaDictList(String requestId, Collection<String> lables);

    /**
     * Получить полное описание справочника
     */
    NsiConfigDict metaDictGet(String requestId, String name);

    /**
     * Получить конфигурацию метаданных
     */
    NsiConfig getConfig();
}
