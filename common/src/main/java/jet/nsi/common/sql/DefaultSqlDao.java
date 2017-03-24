package jet.nsi.common.sql;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.BoolExpBuilder;
import jet.nsi.api.data.BoolExpVisitor;
import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.data.NsiQueryAttr;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.DictRowAttr;
import jet.nsi.api.model.MetaFieldType;
import jet.nsi.api.model.MetaParamValue;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.model.RefAttrsType;
import jet.nsi.api.model.SortExp;
import jet.nsi.api.platform.PlatformSqlDao;
import jet.nsi.api.sql.SqlDao;
import jet.nsi.api.sql.SqlGen;
import jet.nsi.common.data.NsiDataException;
import jet.nsi.common.data.WriteLockNsiDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DefaultSqlDao implements SqlDao {

    private static final Logger log = LoggerFactory
            .getLogger(DefaultSqlDao.class);

    protected PlatformSqlDao platformSqlDao;
    protected SqlGen sqlGen;
    protected DataSource dataSource; //todo в конструктор


    public class SetParamBoolExpVisitor extends BoolExpVisitor {
        protected final NsiQuery query;
        protected final PreparedStatement ps;
        protected int index;

        public SetParamBoolExpVisitor(NsiQuery query, PreparedStatement ps, int index) {
            this.query = query;
            this.ps = ps;
            this.index = index;
        }

        @Override
        protected void visit(BoolExp filter) {
            if (filter.getValue() != null) {
                try {
                    NsiConfigAttr configAttr = query.getDict().getAttr(filter.getKey());
                    List<NsiConfigField> fields = configAttr.getFields();
                    String queryAttrName = configAttr.getName();

                    List<String> dataValues = filter.getValue().getValues();
                    if (filter.getFunc().equalsIgnoreCase(OperationType.IN)) {
                        setInParams(fields, dataValues, queryAttrName);
                    } else {
                        setSimpleParams(filter, fields, dataValues, queryAttrName);
                    }
                } catch (SQLException e) {
                    throw new NsiDataException("visit", e);
                }
            }
        }

        public int getIndex() {
            return index;
        }

        private void setSimpleParams(BoolExp filter, List<NsiConfigField> fields, List<String> dataValues, String queryAttrName) throws SQLException {
            platformSqlDao.checkDataValues(fields, queryAttrName, dataValues);

            int i = 0;
            for (NsiConfigField field : fields) {
                if (dataValues.get(i) != null) {
                    String val = dataValues.get(i);
                    val = wrapFilterFieldValue(filter, field, val);
                    platformSqlDao.setParam(ps, index, field, val);
                    index++;
                    i++;
                }
            }
        }

        private void setInParams(List<NsiConfigField> fields, List<String> dataValues, String queryAttrName) throws SQLException {
            if (fields.size() != 1) {
                throw new IllegalStateException("Multiple fields not supported with 'IN' operator; attrName:" + queryAttrName);
            }
            for (String val : dataValues) {
                platformSqlDao.setParam(ps, index, fields.get(0), val);
                index++;
            }
        }
    }


    protected String wrapFilterFieldValue(BoolExp filter, NsiConfigField field, String val) {
        return platformSqlDao.wrapFilterFieldValue(filter, field, val);
    }

    protected void rsToDictRow(NsiQuery query, ResultSet rs, DictRow result) throws SQLException {
        rsToDictRow(query, rs, result, true);
    }

    protected void rsToDictRow(NsiQuery query, ResultSet rs, DictRow result, boolean includeRefAttrs) throws SQLException {
        rsToDictRow(query, rs, result, includeRefAttrs, RefAttrsType.REF_OBJECT_ATTRS);
    }

    protected void rsToDictRow(NsiQuery query, ResultSet rs, DictRow result, boolean includeRefAttrs, RefAttrsType refAttrsType) throws SQLException {
        int index = 1;
        NsiConfigDict dict = query.getDict();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            DictRowAttr attrValue = new DictRowAttr();
            result.setAttr(attr.getName(), attrValue);

            ArrayList<String> fieldValues = new ArrayList<>(queryAttr.getAttr().getFields().size());
            attrValue.setValues(fieldValues);
            for (NsiConfigField field : attr.getFields()) {
                fieldValues.add(getFieldValue(rs, index, field));
                index++;
            }
            if (includeRefAttrs && dict.isAttrHasRefAttrs(attr)) {
                List<NsiConfigAttr> refAttrs = attr.getRefDict().getRefAttrs(refAttrsType);
                if (refAttrs != null) {
                    Map<String, DictRowAttr> refAttrValues = new HashMap<>(refAttrs.size());
                    attrValue.setRefAttrs(refAttrValues);
                    for (NsiConfigAttr refAttr : refAttrs) {
                        DictRowAttr refAttrValue = new DictRowAttr();
                        refAttrValues.put(refAttr.getName(), refAttrValue);
                        List<String> refAttrFieldValues = new ArrayList<>(refAttr.getFields().size());
                        refAttrValue.setValues(refAttrFieldValues);
                        for (NsiConfigField field : refAttr.getFields()) {
                            refAttrFieldValues.add(getFieldValue(rs, index, field));
                            index++;
                        }
                    }
                }
            }
        }
    }

    protected String getFieldValue(ResultSet rs, int index, NsiConfigField field) throws SQLException {
        return platformSqlDao.getFieldValue(rs, index, field);
    }

    protected String getClobStringValue(Clob clob) throws SQLException {
        return platformSqlDao.getClobStringValue(clob);
    }

    protected String trimTrailing(String value) {
        return value == null ? null : CharMatcher.WHITESPACE.trimTrailingFrom(value);
    }

    public int setParamsForInsert(NsiQuery query, DictRow data, PreparedStatement ps) throws SQLException {
        int index = 1;
        //if(dataAttrMap.size() != query.getAttrs().size()) {
        //    throw new NsiDataException("data and query attr count not match: " + query.getAttrs().size());
        //}
        NsiConfigAttr idAttr = query.getDict().getIdAttr();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();

            List<NsiConfigField> fields = attr.getFields();

            String queryAttrName = attr.getName();
            DictRowAttr dataAttr = data.getAttrs().get(queryAttrName);

            if (idAttr != null
                    && attr.getName().equals(idAttr.getName())
                    && idAttr.getFields().size() == 1
                    && DictRowAttr.isEmpty(dataAttr)) {
                // если у нас вставка и ид атрибут состоит из одного поля и он пуст, пропускаем его,
                // потому что он получается из последовательности
                continue;
            }

            List<String> dataValues;
            if (dataAttr == null) {
                if (attr.getDefaultValue() == null) {
                    throw new NsiDataException("can't find data attr for query attr: " + queryAttrName);
                } else {
                    dataValues = attr.getDefaultValue().getValues();
                }
            } else {
                dataValues = dataAttr.getValues();
            }

            platformSqlDao.checkDataValues(fields, queryAttrName, dataValues);

            int i = 0;
            for (NsiConfigField field : fields) {
                platformSqlDao.setParam(ps, index, field, dataValues.get(i));
                index++;
                i++;
            }
        }
        return index;
    }

    public int setParamsForUpdate(NsiQuery query, DictRow data, PreparedStatement ps) throws SQLException {
        return platformSqlDao.setParamsForUpdate(query, data, ps);
    }

    protected int setParamsForList(NsiQuery query, PreparedStatement ps, BoolExp filter, long offset, int size,
                                   String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) throws SQLException {
        // если заданы параметры для источника то они должны задаваться первыми
        int index = 1;
        if (sourceQueryParams != null) {
            index = setParamsForSourceQuery(query, ps, index, sourceQueryName, sourceQueryParams);
        }
        // затем параметры для фильтра
        index = setParamsForFilter(query, ps, index, filter);
        // затем ограничение выборки
        index = platformSqlDao.limit(ps, index, offset, size);

        return index;
    }


    protected int setParamsForSourceQuery(NsiQuery query, PreparedStatement ps,
                                          int index, String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) throws SQLException {
        for (MetaParamValue p : sourceQueryParams) {
            String v = p.getValue();
            platformSqlDao.setParam(ps, index, p.getType(), v != null ? v.length() : 0, v);
            index++;
        }
        return index;
    }

    protected int setParamsForFilter(NsiQuery query, PreparedStatement ps, int index, BoolExp filter) {
        if (filter == null) {
            return index;
        }
        SetParamBoolExpVisitor visitor = new SetParamBoolExpVisitor(query, ps, index);
        if (filter != null) {
            visitor.accept(filter);
        }
        return visitor.getIndex();
    }

    protected void rsToDictRowIdAttr(NsiQuery query, ResultSet rs,
                                     DictRow data) throws SQLException {
        List<NsiConfigField> fields = query.getDict().getIdAttr().getFields();
        NsiConfigAttr idAttr = query.getDict().getIdAttr();
        DictRowAttr idAttrValue = data.getAttrs().get(idAttr.getName());
        if (idAttrValue == null) {
            idAttrValue = new DictRowAttr();
            data.getAttrs().put(idAttr.getName(), idAttrValue);
        }
        ArrayList<String> fieldValues = new ArrayList<String>(fields.size());
        idAttrValue.setValues(fieldValues);
        int index = 1;
        for (NsiConfigField field : fields) {
            fieldValues.add(getFieldValue(rs, index, field));
            index++;
        }
    }

    protected long getCountFromRs(NsiQuery query, ResultSet rs) throws SQLException {
        return platformSqlDao.getCountFromRs(query, rs);
    }

    public void setSqlGen(SqlGen sqlGen) {
        this.sqlGen = sqlGen;
    }

    @Override
    public DictRow get(Connection connection, NsiQuery query,
                       DictRowAttr id, BoolExp filter) {
        return get(connection, query, id, false, filter);
    }

    @Override
    public DictRow get(Connection connection, NsiQuery query,
                       DictRowAttr id, boolean lock, BoolExp filter) {
        return get(connection, query, id, lock, RefAttrsType.REF_OBJECT_ATTRS, filter);
    }

    @Override
    public DictRow get(Connection connection, NsiQuery query,
                       DictRowAttr id, boolean lock, RefAttrsType refAttrsType, BoolExp filter) {
        NsiConfigDict dict = query.getDict();
        checkDictHasIdAttr(dict);
        DictRow result = dict.newDictRow();
        filter = buildIdFilter(dict, id, filter) ;
        String sql = sqlGen.getRowGetSql(query, lock, filter );

        log.info(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramCount = setParamsForFilter(query, ps, 1, filter );

            log.info("params: {}", paramCount);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rsToDictRow(query, rs, result, !lock, refAttrsType);
                }
            }
        } catch (Exception e) {
            throw new NsiDataException("get:" + e.getMessage(), e);
        }
        return result;
    }

    private BoolExp buildIdFilter(NsiConfigDict dict, DictRowAttr id, BoolExp sourceFilter) {
        BoolExpBuilder builder = new BoolExpBuilder(dict)
                .and()
                .expList()
                .key(dict.getIdAttr().getName()).eq().value(id).add();
        if (sourceFilter != null) {
            builder.add(sourceFilter);
        }
        return builder.end().build();
    }

    @Override
    public List<DictRow> list(Connection connection, NsiQuery query,
                              BoolExp filter, List<SortExp> sortList, long offset, int size) {
        return list(connection, query, filter, sortList, offset, size, null, null);
    }

    @Override
    public List<DictRow> list(Connection connection, NsiQuery query,
                              BoolExp filter, List<SortExp> sortList, long offset, int size,
                              String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) {
        return list(connection, query, filter, sortList, offset, size, sourceQueryName, sourceQueryParams, RefAttrsType.REF_OBJECT_ATTRS);
    }

    @Override
    public List<DictRow> list(Connection connection, NsiQuery query,
                              BoolExp filter, List<SortExp> sortList, long offset, int size,
                              String sourceQueryName, Collection<MetaParamValue> sourceQueryParams, RefAttrsType refAttrsType) {
        List<DictRow> result = new ArrayList<>();
        String sql = sqlGen.getListSql(query, filter, sortList, offset, size, sourceQueryName, refAttrsType);
        log.info(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramCount = setParamsForList(query, ps, filter, offset, size, sourceQueryName, sourceQueryParams) - 1;
            log.info("params: {}", paramCount);
            if (ps.execute()) {
                try (ResultSet rs = ps.getResultSet()) {
                    while (rs.next()) {
                        DictRow data = query.getDict().newDictRow();
                        rsToDictRow(query, rs, data, true, refAttrsType);
                        result.add(data);
                    }
                }
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new NsiDataException("list:" + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public long count(Connection connection, NsiQuery query, BoolExp filter) {
        return count(connection, query, filter, null, null);
    }

    @Override
    public long count(Connection connection, NsiQuery query, BoolExp filter,
                      String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) {
        String sql = sqlGen.getCountSql(query, filter, sourceQueryName);
        log.info(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            if (sourceQueryParams != null) {
                index = setParamsForSourceQuery(query, ps, index, sourceQueryName, sourceQueryParams);
            }
            int paramCount = setParamsForFilter(query, ps, index, filter) - 1;
            log.info("params: {}", paramCount);

            if (ps.execute()) {
                try (ResultSet rs = ps.getResultSet()) {
                    if (rs.next()) {
                        return getCountFromRs(query, rs);
                    } else {
                        throw new RuntimeException();
                    }
                }
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new NsiDataException("count:" + e.getMessage(), e);
        }
    }

    @Override
    public DictRow insert(Connection connection, NsiQuery query, DictRow data) {
        checkDictHasIdAttr(query.getDict());
        setVersionDefault(query, data);
        if (platformSqlDao.useUUIDForId()) {
            data.setIdAttr(UUID.randomUUID().toString());
        }
        NsiConfigAttr idAttr = query.getDict().getIdAttr();
        boolean useSeq = (idAttr != null && idAttr.getFields().size() == 1 && DictRowAttr.isEmpty(data.getIdAttr()));

        String sql = sqlGen.getRowInsertSql(query, useSeq);

        log.info(sql);
        String idField = platformSqlDao.getFieldSpelling(query.getDict().getIdAttr().getFields().get(0).getName());
        try (PreparedStatement ps = connection.prepareStatement(sql,
                new String[]{idField})) {
            int paramCount = setParamsForInsert(query, data, ps) - 1;
            log.info("params: {}", paramCount);

            ps.execute();
            if (useSeq) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        rsToDictRowIdAttr(query, rs, data);
                    } else {
                        throw new NsiDataException("not found");
                    }
                }
            } else {
                DictRow row = query.getDict().newDictRow();
                row.setIdAttr(data.getIdAttr());
                return row;
            }
            return get(connection, query, data.getIdAttr(), null);
        } catch (SQLException e) {
            throw new NsiDataException("insert:" + e.getMessage(), e);
        }
    }

    private void setVersionDefault(NsiQuery query, DictRow data) {
        NsiConfigDict dict = query.getDict();
        if (dict.getVersionAttr() != null) {
            query.addVersion();
            if (data.isAttrEmpty(dict.getVersionAttr())) {
                data.setAttr(dict.getVersionAttr().getName(), 1);
            }
        }
    }

    private void updateRowData(DictRow row, DictRow data) throws SQLException {
        for (String attrName : row.getAttrs().keySet()) {
            if (!data.getAttrs().containsKey(attrName)) {
                data.setAttr(attrName, row.getAttr(attrName));
            }
        }
    }

    @Override
    public boolean delete(Connection connection, NsiQuery query, DictRow data, BoolExp filter) {
        filter = buildIdFilter(query.getDict(), data.getIdAttr(), filter);
        String sql = sqlGen.getRowDeleteSql(query, filter);
        log.info(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParamsForFilter(query, ps, 1, filter);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if (platformSqlDao.getForeignKeyViolationSqlState().equals(e.getSQLState())) {
                throw new NsiDataException("sqlError.foreignKeyViolation", e);
            }
            throw new NsiDataException("delete:" + e.getMessage(), e);
        }
    }

    @Override
    public DictRow update(Connection connection, NsiQuery query,
                          DictRow data, BoolExp filter) {
        checkDictHasIdAttr(query.getDict());
        NsiConfigDict dict = query.getDict();
        filter = buildIdFilter(dict, data.getIdAttr(), filter) ;

        String sql = sqlGen.getRowUpdateSql(query, filter);
        log.info(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (query.getAttrs().size() != data.getAttrs().size()) {
                DictRow row = get(connection, query, data.getIdAttr(), filter);
                updateRowData(row, data);
            }
            int paramCount;
            // ставим эксклюзивную блокировку на запись
            DictRow curData = getCurDataLock(connection, dict, data);

            if (dict.getVersionAttr() != null) {
                // если версия в базе задана
                if (!curData.isAttrEmpty(dict.getVersionAttr())) {
                    if (data.isAttrEmpty(dict.getVersionAttr())) {
                        throw new NsiDataException("versionAttr required for update: " + dict.getName());
                    }
                    // проверяем что никто не изменил запись
                    if (!data.getVersionAttrString().equals(curData.getVersionAttrString())) {
                        throw new WriteLockNsiDataException(
                                // получаем полное текущее состояние
                                get(connection, dict.query().addAttrs(), data.getIdAttr(), null)
                                , "record version missmatch: " + dict.getName() + ", your/db versions are " + data.getVersionAttrString() + "/" + curData.getVersionAttrString())
                                .localize("Редактируемые данные уже были изменены в другой сессии. Сохранение невозможно без повторного обновления. Обновите запись и отредактируйте ее снова.");
                    }
                    // задаем новую версию
                    if (dict.getVersionAttr().getFields().get(0).getType() == MetaFieldType.NUMBER) {
                        data.setVersionAttr(curData.getVersionAttrLong() + 1);
                    } else {
                        // TODO реализовать стратегию
                        throw new NsiDataException("unsupported version type " + dict.getName());
                    }
                } else {
                    // для существующих записей с незаданным значением версии при первом изменении устанавливаем версию в начальное значение
                    setVersionDefault(query, data);
                }
            }
            paramCount = setParamsForUpdate(query, data, ps) ;
            paramCount = setParamsForFilter(query, ps, paramCount, filter )-1;

            log.info("params: {}", paramCount);

            int count = ps.executeUpdate();
            if (count == 0) {
                throw new NsiDataException("row not updated");
            }
            if (count > 1) {
                throw new NsiDataException(Joiner.on(" ").join("too many row updated:", count));
            }
            return get(connection, query, data.getIdAttr(), null);
        } catch (SQLException e) {
            throw new NsiDataException("update:" + e.getMessage(), e);
        }
    }

    private DictRow getCurDataLock(Connection connection, NsiConfigDict dict, DictRow data) {
        DictRow result = null;
        if (dict.getVersionAttr() != null) {
            NsiQuery query = dict.query()
                    .addId();
            if (dict.getVersionAttr() != null) {
                query.addVersion();
            }
            result = get(connection, query, data.getIdAttr(), true, null);
        }
        return result;
    }

    @Override
    public DictRow save(Connection connection, NsiQuery query, DictRow data,
                        boolean insert, BoolExp filter) {
        checkDictHasIdAttr(query.getDict());
        checkUniqueAttr(connection, data, insert);

        DictRow result;
        if (insert) {
            result = insert(connection, query, data);
        } else {
            result = update(connection, query, data, filter);
        }
        return result;
    }

    private void checkUniqueAttr(Connection connection, DictRow data, boolean insert) {
        // TODO - сделать проверку уникальности распеределенной
        List<NsiConfigAttr> configAttrs = data.getDict().getUniqueAttr();
        if (configAttrs == null || configAttrs.size() == 0 || (data.getDeleteMarkAttrBoolean()!=null && data.getDeleteMarkAttrBoolean())) {
            return;
        }

        Map<String, DictRowAttr> rowAttrs = data.getUniqueAttrs();
        log.debug(String.valueOf(rowAttrs));

        boolean isExistAllAttr = true;
        boolean isExistAttr = false;
        for (DictRowAttr value : rowAttrs.values()) {
            if (value == null || value.isEmpty()) {
                isExistAllAttr = false;
            } else {
                isExistAttr = true;
            }
        }

        if (insert && !isExistAllAttr || (!insert && isExistAttr && !isExistAllAttr)) {
            throw new NsiServiceException("Атрибуты " + NsiConfigDict.formatAttrs(data.getDict().getName(), configAttrs, ", ") + "обязательные");
        }

        if (!isExistAllAttr) {
            return;
        }

        BoolExpBuilder fb = data.getDict().filter().and().expList();
        fb.uniqueAttr(rowAttrs);
        fb.deleteMark(false).add();
        if (!insert) {
            fb.key(data.getDict().getIdAttr().getName()).notEq().value(data.getIdAttr()).add();
        }

        long count = count(connection, data.getDict().query().addUniqueAttrs(), fb.end().build());

        if (count > 0) {
            if (configAttrs.size() == 1) {
                NsiConfigAttr configAttr = configAttrs.get(0);
                throw new NsiServiceException("Значение атрибута  " + data.getDict().getName() + "." + configAttr.getName() + " должно быть уникальным. Нарушено ограничение для значения " + rowAttrs.get(configAttr.getName()).getValues());
            } else {
                throw new NsiServiceException("Значения атрибутов  " +
                        NsiConfigDict.formatAttrs(data.getDict().getName(), configAttrs, ", ") +
                        " должны быть уникальными. Нарушено ограничение для значений " + DictRowAttr.formatAttrs(configAttrs, rowAttrs, ", "));
            }
        }
    }

    protected DictRow getSingleRow(final Connection conn, NsiQuery query, final BoolExp filter) {
        List<DictRow> rows = list(conn, query, filter, null, -1, -1, null, null);

        if (rows.size() == 0) {
            return null;
        } else if (rows.size() == 1) {
            return rows.get(0);
        } else {
            throw new NsiServiceException("Найдено " + rows.size() + " строк в " + query.getDict().getName() + " соответствующих условию %s " + filter.toString());
        }
    }

    protected DictRow getByExternalAttrs(final Connection connection, final DictRow data) {
        NsiConfigDict dict = data.getDict();
        List<NsiConfigAttr> mAttrs = dict.getMergeExternalAttrs();
        Preconditions.checkArgument(mAttrs != null && mAttrs.size() > 0, "mergeExternalAttrs не заданы для %s", dict.getName());

        BoolExpBuilder fb = dict.filter().and().expList();
        if (dict.getOwnerAttr() != null && data.getOwnerAttr() != null) {
            fb.key(dict.getOwnerAttr().getName()).eq().value(data.getOwnerAttr()).add();
        }
        for (NsiConfigAttr configAttr : mAttrs) {
            DictRowAttr rowAttr = data.getAttr(configAttr.getName());
            Preconditions.checkNotNull(rowAttr, "Атрибут %s не существует в %s", configAttr.getName(), dict.getName());
            fb.key(configAttr.getName()).eq().value(rowAttr).add();
        }
        BoolExp filter = fb.end().build();

        return getSingleRow(connection, dict.query().addAttrs(), filter);
    }

    private String buildConditionString(Map<String, DictRowAttr> entry) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, DictRowAttr> attr : entry.entrySet()) {
            sb.append(attr.getKey()).append("=");
            for (String value : attr.getValue().getValues()) {
                sb.append(value).append(",");
            }
            sb.append(" ");
        }

        return sb.toString();
    }

    private DictRow mergeDictRow(DictRow source, DictRow target) {
        NsiConfigDict dict = target.getDict();
        NsiConfigAttr idAttr = dict.getIdAttr();
        Preconditions.checkNotNull(idAttr, "Нет idAttr в %s", dict.getName());

        for (Map.Entry<String, DictRowAttr> entry : source.getAttrs().entrySet()) {
            if (idAttr.getName().equals(entry.getKey())) {
                continue;
            }
            // если есть versionAttr то его тоже пропускаем
            if (dict.getVersionAttr() != null
                    && dict.getVersionAttr().getName().equals(entry.getKey())) {
                continue;
            }
            target.setAttr(entry.getKey(), entry.getValue());
        }

        return target;
    }

    @Override
    public DictRow mergeByExternalAttrs(Connection connection, DictRow data) {
        throw new UnsupportedOperationException("mergeByExternalAttrs not supported in antifraud system");
/*        NsiConfigDict dict = data.getDict();

        for (Map.Entry<String, DictRowAttr> entryAttr : data.getAttrs().entrySet()) {
            NsiConfigAttr configAttr = dict.getAttr(entryAttr.getKey());
            if (dict.isAttrHasRefAttrs(configAttr) && entryAttr.getValue().getValues() == null) {
                Preconditions.checkNotNull(entryAttr.getValue().getRefAttrs(), "refAttrs не заданы для атрибута %s в %s", entryAttr.getKey(), dict.getName());

                List<NsiConfigAttr> mAttrs = configAttr.getRefDict().getMergeExternalAttrs();
                Preconditions.checkArgument(mAttrs != null && mAttrs.size() > 0, "mergeExternalAttrs не заданы для %s", configAttr.getRefDict().getName());

                BoolExpBuilder fb = configAttr.getRefDict().filter().and().expList();
                for (NsiConfigAttr mConfigAttr : mAttrs) {
                    DictRowAttr refAttr = entryAttr.getValue().getRefAttrs().get(mConfigAttr.getName());
                    Preconditions.checkNotNull(refAttr, "Значение для refAttr %s в %s не задано", mConfigAttr.getName(), dict.getName());
                    fb.key(mConfigAttr.getName()).eq().value(refAttr).add();
                }
                BoolExp filter = fb.end().build();

                DictRow refRow = getSingleRow(connection, configAttr.getRefDict().query().addAttrs(), filter);
                Preconditions.checkNotNull(refRow, "Не найдена запись в %s соответствующая условию %s",
                        configAttr.getRefDict().getName(), buildConditionString(entryAttr.getValue().getRefAttrs()));
                entryAttr.getValue().setValues(refRow.getIdAttr().getValues());
                entryAttr.getValue().setRefAttrs(null);
            }
        }

        DictRow oldRow = getByExternalAttrs(connection, data);
        DictRow newRow;

        if (oldRow == null) {
            data.builder().idAttrNull();
            newRow = insert(connection, dict.query().addAttrs(data), data);
        } else {
            newRow = mergeDictRow(data, oldRow);
            newRow = update(connection, dict.query().addAttrs(newRow), newRow, filter);
        }

        return newRow;*/
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void checkDictHasIdAttr(NsiConfigDict dict) {
        Preconditions.checkNotNull(dict.getIdAttr(), "dict %s have't id attr", (Object) dict.getName());
    }

    public void setPlatformSqlDao(PlatformSqlDao platformSqlDao) {
        this.platformSqlDao = platformSqlDao;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
