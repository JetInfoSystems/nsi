package jet.nsi.api;

import java.util.Collection;
import java.util.List;

import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.DictRowAttr;
import jet.nsi.api.model.MetaParamValue;
import jet.nsi.api.model.SortExp;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.sql.SqlDao;
import jet.nsi.api.tx.NsiTransaction;

public interface NsiGenericService {

    NsiPlatform getNsiPlatform();
    
    /**
     * Получить количество записей справочника соответствующих заданному условию
     */
    long dictCount(String requestId, NsiQuery query, BoolExp filter, SqlDao sqlDao);

    /**
     * Получить количество записей справочника соответствующих заданному условию
     */
    long dictCount(String requestId, NsiQuery query, BoolExp filter, SqlDao sqlDao,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams);
    /**
     * Получить страницу списка записей справочника соответствующих заданному условию
     */
    List<DictRow> dictList(String requestId, NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size, SqlDao sqlDao );

    /**
     * Получить страницу списка записей справочника соответствующих заданному условию
     */
    List<DictRow> dictList(String requestId, NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size, SqlDao sqlDao,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams );
    /**
     * Получить полное состояние строки справочника, со всеми атрибутами
     */
    DictRow dictGet(String requestId, NsiConfigDict dict, DictRowAttr id, SqlDao sqlDao, BoolExp filter);

    /**
     * Сохранить состояние записи справочника, если ид атрибут задан то обновление, если нет то создание
     */
    DictRow dictSave(String requestId, DictRow data, SqlDao sqlDao, BoolExp filter);

    /**
     * Сохранить состояние нескольких записей справочника, если ид атрибут задан то обновление, если нет то создание
     * Записи сохраняются в одной транзакции.
     */
    @Deprecated
    List<DictRow> dictBatchSave(String requestId, List<DictRow> dataList, SqlDao sqlDao);

    /**
     * Изменить отметку о удалении для заданной записи справочника
     */
    DictRow dictDelete(String requestId, NsiConfigDict dict, DictRowAttr id, Boolean value, SqlDao sqlDao, BoolExp filter);

    /**
     * Обьединение записи по значению атрибута внешнего ключа
     */
    @Deprecated
    DictRow dictMergeByExternalAttrs(String requestId, final DictRow data, final SqlDao sqlDao);
}
