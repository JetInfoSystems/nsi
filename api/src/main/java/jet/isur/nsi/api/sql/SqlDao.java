package jet.isur.nsi.api.sql;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaParamValue;
import jet.isur.nsi.api.model.RefAttrsType;
import jet.isur.nsi.api.model.SortExp;

public interface SqlDao {

    /**
     * Получить запись справочника, если есть REF атрибуты то для низ возвращаютмя ref атрибуты
     * @param connection
     * @param query
     * @param id
     * @return
     */
    public DictRow get(Connection connection, NsiQuery query, DictRowAttr id);

    /**
     * Получить запись справочника, если есть REF атрибуты то для низ возвращаютмя ref атрибуты
     * @param connection
     * @param query
     * @param id
     * @param lock взять эксклюзивную блокировку для записи
     * @return
     */
    public DictRow get(Connection connection, NsiQuery query, DictRowAttr id, boolean lock);

    /**
     * Получить запись справочника, если есть REF атрибуты то для низ возвращаютмя ref атрибуты
     * @param connection
     * @param query
     * @param id
     * @param lock взять эксклюзивную блокировку для записи
     * @param refAttrsType тип заполнения ссылочных атрибутов
     * @return
     */
    public DictRow get(Connection connection, NsiQuery query, DictRowAttr id, boolean lock, RefAttrsType refAttrsType);

    /**
     * Получить страницу записей справочника удовлетворяющих критерию,
     * если есть REF атрибуты то для низ возвращаютмя ref атрибуты
     * @param connection
     * @param query
     * @param filter
     * @param sortList
     * @param offset
     * @param size
     * @return
     */
    public List<DictRow> list(Connection connection, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size);

    public List<DictRow> list(Connection connection, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams);

    public List<DictRow> list(Connection connection, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams, RefAttrsType refAttrsType);

    /**
     * Получить количество записей справочника, удовлетворяющих критерию,
     * если есть REF атрибуты то для низ возвращаютмя ref атрибуты
     * @param connection
     * @param query
     * @param filter
     * @return
     */
    public long count(Connection connection, NsiQuery query, BoolExp filter);

    public long count(Connection connection, NsiQuery query, BoolExp filter,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams);

    /**
     * Вставить запись, ref атрибуты для ссылосных атрибутов не получаются
     * @param connection
     * @param query
     * @param data
     * @return
     */
    public DictRow insert(Connection connection, NsiQuery query, DictRow data);

    /**
     * Обновить запись, ref атрибуты для ссылочных атрибутов не возвращаются
     * @param connection
     * @param query
     * @param data
     * @return
     */
    public DictRow update(Connection connection, NsiQuery query, DictRow data);

    /**
     * Вставить или обновить запись, возвращаются ref атрибуты для ссылочных атрибутов
     * @param connection
     * @param query
     * @param data
     * @param insert
     * @return
     */
    public DictRow save(Connection connection, NsiQuery query, DictRow data, boolean insert);

    /**
     * Обьединение записей
     */
	public DictRow mergeByExternalAttrs(Connection connection, DictRow data);

}
