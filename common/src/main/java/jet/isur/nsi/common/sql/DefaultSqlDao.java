package jet.isur.nsi.common.sql;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.BoolExpBuilder;
import jet.isur.nsi.api.data.BoolExpVisitor;
import jet.isur.nsi.api.data.ConvertUtils;
import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.DictRowBuilder;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.MetaParamValue;
import jet.isur.nsi.api.model.OperationType;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.sql.SqlDao;
import jet.isur.nsi.api.sql.SqlGen;
import jet.isur.nsi.common.data.NsiDataException;

public class DefaultSqlDao implements SqlDao {

    private static final Logger log = LoggerFactory
            .getLogger(DefaultSqlDao.class);

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
            if(filter.getValue() != null) {
                try {
                    NsiConfigAttr configAttr = query.getDict().getAttr(filter.getKey());
                    List<NsiConfigField> fields = configAttr.getFields();
                    String queryAttrName = configAttr.getName();

                    List<String> dataValues = filter.getValue().getValues();
                    checkDataValues(fields, queryAttrName, dataValues);

                    int i = 0;
                    for (NsiConfigField field : fields) {
                        if (dataValues.get(i) != null){
                            String val = dataValues.get(i);
                            if (filter.getFunc().equals(OperationType.LIKE))
                            {
                                val = val+"%";
                            }
                            setParam(ps, index, field, val);
                            index++;
                            i++;
                        }
                    }
                } catch(SQLException e) {
                    throw new NsiDataException("visit",e);
                }
            }
        }

        public int getIndex() {
            return index;
        }
    }

    protected SqlGen sqlGen;

    public void rsToDictRow(NsiQuery query, ResultSet rs, DictRow result) throws SQLException {
        int index = 1;
        NsiConfigDict dict = query.getDict();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            DictRowAttr attrValue = new DictRowAttr();
            result.setAttr(attr.getName(), attrValue);
            
            ArrayList<String> fieldValues = new ArrayList<>(queryAttr.getAttr().getFields().size());
            attrValue.setValues(fieldValues);
            for (NsiConfigField field : attr.getFields()) {
                fieldValues.add(getFieldValue(rs,index,field));
                index++;
            }
            if(dict.isAttrHasRefAttrs(attr)) {
                int refAttrCount = attr.getRefDict().getRefObjectAttrs().size();
                Map<String, DictRowAttr> refAttrValues = new HashMap<>(refAttrCount);
                attrValue.setRefAttrs(refAttrValues);
                for (NsiConfigAttr refAttr : attr.getRefDict().getRefObjectAttrs()) {
                    DictRowAttr refAttrValue = new DictRowAttr();
                    refAttrValues.put(refAttr.getName(), refAttrValue);
                    List<String> refAttrFieldValues = new ArrayList<>(refAttr.getFields().size());
                    refAttrValue.setValues(refAttrFieldValues);
                    for (NsiConfigField field : refAttr.getFields() ) {
                        refAttrFieldValues.add(getFieldValue(rs,index,field));
                        index++;
                    }
                }
            }
        }
    }

    protected String getFieldValue(ResultSet rs, int index, NsiConfigField field) throws SQLException {
        // TODO нужен внутренний интерфейс без конверсии в строки
        switch (field.getType()) {
        case BOOLEAN:
            String boolValue = rs.getString(index);
            return ConvertUtils.boolToString(ConvertUtils.dbStringToBool(boolValue));
        case DATE_TIME:
            Timestamp dateValue = rs.getTimestamp(index);
            return ConvertUtils.timestampToString(dateValue);
        case NUMBER:
            BigDecimal bigDecimalValue = rs.getBigDecimal(index);
            return ConvertUtils.bigDecimalToString(bigDecimalValue);
        case VARCHAR:
            return rs.getString(index);
        case CHAR:
            return trimTrailing(rs.getString(index));
        case CLOB:
            return getClobStringValue(rs.getClob(index));
        default:
            throw new NsiDataException("unsupported field type: " + field.getType());
        }
    }

    protected String getClobStringValue(Clob clob) throws SQLException {
        return clob.getSubString(1, (int) clob.length());
    }

    protected String trimTrailing(String value) {
        return value == null ? null : CharMatcher.WHITESPACE.trimTrailingFrom(value);
    }

    public int setParamsForInsert(NsiQuery query, DictRow data,
            PreparedStatement ps) throws SQLException {
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

            if(idAttr != null
                    && attr.getName().equals(idAttr.getName())
                    && idAttr.getFields().size() == 1
                    && DictRowAttr.isEmpty(dataAttr)) {
                // если у нас вставка и ид атрибут состоит из одного поля и он пуст, пропускаем его,
                // потому что он получается из последовательности
                continue;
            }
            if(dataAttr == null) {
                throw new NsiDataException("can't find data attr for query attr: " + queryAttrName);
            }
            
            List<String> dataValues = dataAttr.getValues();
            checkDataValues(fields, queryAttrName, dataValues);

            int i = 0;
            for (NsiConfigField field : fields) {
                setParam(ps, index, field, dataValues.get(i));
                index++;
                i++;
            }
        }
        return index;
    }

    public int setParamsForUpdate(NsiQuery query, DictRow data,
            PreparedStatement ps) throws SQLException {
        int index = 1;
        NsiConfigAttr idAttr = query.getDict().getIdAttr();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();

            List<NsiConfigField> fields = attr.getFields();

            String queryAttrName = attr.getName();
            DictRowAttr dataAttr = data.getAttrs().get(queryAttrName);
            // пропускаем id
            if (attr == idAttr) {
                continue;
            }

            if(dataAttr == null) {
                throw new NsiDataException(
                        "can't find data attr for query attr: " + queryAttrName);
            }
            List<String> dataValues = dataAttr.getValues();
            checkDataValues(fields, queryAttrName, dataValues);
            int i = 0;
            for (NsiConfigField field : fields) {
                setParam(ps, index, field, dataValues.get(i));
                index++;
                i++;
            }
        }

        // для обновления дополнительные параметры для where условия
        List<NsiConfigField> fields = idAttr.getFields();
        DictRowAttr attrValue = data.getAttrs().get(idAttr.getName());
        List<String> dataValues = attrValue.getValues();
        checkDataValues(fields, idAttr.getName(), dataValues);

        int i=0;
        for (NsiConfigField field : fields) {
            setParam(ps, index, field, dataValues.get(i));
            index++;
            i++;
        }
        return index;
    }

    public int setParamsForList(NsiQuery query, PreparedStatement ps, BoolExp filter, long offset, int size) throws SQLException {
           return setParamsForList(query, ps, filter, offset, size, null, null);
    }

    public int setParamsForList(NsiQuery query, PreparedStatement ps, BoolExp filter, long offset, int size,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams ) throws SQLException {
        // если заданы параметры для источника то они должны задаваться первыми
        int index = 1;
        if(sourceQueryParams != null) {
            index = setParamsForSourceQuery(query, ps, index, sourceQueryName, sourceQueryParams);
        }
        // затем параметры для фильтра
        index = setParamsForFilter(query, ps, index, filter);
        // затем огарничение выборки
        if (offset != -1 && size != -1){
            ps.setLong(index++, offset+size+1);
            ps.setLong(index++, offset+1);
        }
        return index;
 }

    protected int setParamsForSourceQuery(NsiQuery query, PreparedStatement ps,
            int index, String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) throws SQLException {
        for (MetaParamValue p : sourceQueryParams) {
            String v = p.getValue();
            setParam(ps, index, p.getType(), v != null ? v.length() : 0, v);
            index++;
        }
        return index;
    }

    protected int setParamsForFilter(NsiQuery query, PreparedStatement ps, int index,
            BoolExp filter) {
        SetParamBoolExpVisitor visitor = new SetParamBoolExpVisitor(query, ps, index);
        if(filter != null) {
            visitor.accept(filter);
        }
        return visitor.getIndex();
    }

    protected void checkDataValues(List<NsiConfigField> fields,
            String queryAttrName, List<String> dataValues) {
        if (dataValues == null) {
            throw new NsiDataException("empty values in data attr: "
                    + queryAttrName);
        }
        if (dataValues.size() != fields.size()) {
            throw new NsiDataException(
                    "data values and query attr fields count not match");
        }
    }

    public void rsToDictRowIdAttr(NsiQuery query, ResultSet rs,
            DictRow data) throws SQLException {
        List<NsiConfigField> fields = query.getDict().getIdAttr().getFields();
        NsiConfigAttr idAttr = query.getDict().getIdAttr();
        DictRowAttr idAttrValue = data.getAttrs().get(idAttr.getName());
        if(idAttrValue == null) {
            idAttrValue = new DictRowAttr();
            data.getAttrs().put(idAttr.getName(), idAttrValue);
        }
        ArrayList<String> fieldValues = new ArrayList<String>(fields.size());
        idAttrValue.setValues(fieldValues);
        int index = 1;
        for (NsiConfigField field : fields) {
            fieldValues.add(getFieldValue(rs, index, field));
            index ++;
        }
    }

    public long getCountFromRs(NsiQuery query, ResultSet rs) throws SQLException {
        return rs.getBigDecimal(1).longValue();
    }

    public void setSqlGen(SqlGen sqlGen) {
        this.sqlGen = sqlGen;
    }

    public DictRow get(Connection connection, NsiQuery query,
            DictRowAttr id) {
        NsiConfigDict dict = query.getDict();
        checkDictHasIdAttr(dict);
        DictRow result = dict.newDictRow();
        String sql = sqlGen.getRowGetSql(query);
        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramCount = setParamsForGetWhere(query, ps, id) - 1;
            log.info("params: {}", paramCount);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    rsToDictRow(query, rs, result);
                } else {
                    throw new NsiDataException(Joiner.on(" ").join("not foud", id));
                }
            }
        } catch (Exception e) {
            throw new NsiDataException("get:" + e.getMessage(),e);
        }
        return result;
    }

    private void checkDictHasIdAttr(NsiConfigDict dict) {
        Preconditions.checkNotNull(dict.getIdAttr(), "dict %s have't id attr", (Object)dict.getName());
    }

    /**
     * Биндит к запросу значения полей, составляющих id (primary key). Возвращает индекс следующего placeholder-а
     */
    protected int setParamsForGetWhere(NsiQuery query, PreparedStatement ps,
            DictRowAttr id) throws SQLException {
        List<NsiConfigField> fields = query.getDict().getIdAttr().getFields();
        for(int i=0;i<fields.size();i++) {
            NsiConfigField field = fields.get(i);
            setParam(ps,i+1,field,id.getValues().get(i));
        }
        return fields.size() + 1;
    }

    /**
     * Биндит к запросу значение поля под заданным индексом
     */
    protected void setParam(PreparedStatement ps, int index, NsiConfigField field,
            String value) throws SQLException {
        setParam(ps, index, field.getType(), field.getSize(), field.getPrecision(), value);
    }

    protected void setParam(PreparedStatement ps, int index, MetaFieldType fieldType, int fieldSize,
            String value) throws SQLException {
        setParam(ps, index, fieldType, fieldSize, 0, value);
    }

    protected void setParam(PreparedStatement ps, int index, MetaFieldType fieldType, int fieldSize, int fieldPrecision,
            String value) throws SQLException {
        switch (fieldType) {
        case BOOLEAN:
            if(Strings.isNullOrEmpty(value)) {
                ps.setNull(index, Types.VARCHAR);
            } else {
                ps.setString(index, ConvertUtils.dbBoolToString(ConvertUtils.stringToBool(value)));
            }
            break;
        case NUMBER:
            if(Strings.isNullOrEmpty(value)) {
                ps.setNull(index, Types.BIGINT);
            } else {
                if(fieldPrecision > 0) {
                    ps.setBigDecimal(index, new BigDecimal(value));
                } else if(fieldSize <= 19) {
                    ps.setLong(index, Long.parseLong(value));
                } else {
                    ps.setBigDecimal(index, new BigDecimal(value));
                }
            }
            break;
        case DATE_TIME:
            if(Strings.isNullOrEmpty(value)) {
                ps.setNull(index, Types.DATE);
            } else {
                DateTime dateTime = ConvertUtils.stringToDateTime(value);
                ps.setTimestamp(index, new Timestamp(dateTime.getMillis()));
            }
            break;
        case VARCHAR:
            if(Strings.isNullOrEmpty(value)) {
                ps.setNull(index, Types.VARCHAR);
            } else {
                ps.setString(index, value);
            }
            break;
        case CHAR:
            if(Strings.isNullOrEmpty(value)) {
                ps.setNull(index, Types.CHAR);
            } else {
                ps.setString(index, Strings.padEnd(value, fieldSize, ' '));
            }
            break;
        case CLOB:
            if(Strings.isNullOrEmpty(value)) {
                ps.setNull(index, Types.CLOB);
            } else {
                Clob clob = ps.getConnection().createClob();
                clob.setString(1, value);
                ps.setClob(index, clob);
            }
            break;
        default:
            throw new NsiDataException(Joiner.on(" ").join("unsupported param type:",fieldType));
        }
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
        List<DictRow> result = new ArrayList<>();
        String sql = sqlGen.getListSql(query, filter, sortList, offset, size, sourceQueryName);
        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramCount = setParamsForList(query, ps, filter, offset, size, sourceQueryName, sourceQueryParams) - 1;
            log.info("params: {}", paramCount);
            if(ps.execute()) {
                try(ResultSet rs = ps.getResultSet()) {
                    while(rs.next()) {
                        DictRow data = query.getDict().newDictRow();
                        rsToDictRow(query, rs, data);
                        result.add(data);
                    }
                }
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new NsiDataException("list:" + e.getMessage(),e);
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
        try(PreparedStatement ps = connection.prepareStatement(sql )) {
            int index = 1;
            if(sourceQueryParams != null) {
                index = setParamsForSourceQuery(query, ps, index, sourceQueryName, sourceQueryParams);
            }
            int paramCount = setParamsForFilter(query, ps, index, filter) - 1;
            log.info("params: {}", paramCount);

            if(ps.execute()) {
                try(ResultSet rs = ps.getResultSet()) {
                    if(rs.next()) {
                        return getCountFromRs(query, rs);
                    } else {
                        throw new RuntimeException();
                    }
                }
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new NsiDataException("count:" + e.getMessage(),e);
        }
    }


    public DictRow insert(Connection connection, NsiQuery query, DictRow data) {
        checkDictHasIdAttr(query.getDict());

        NsiConfigAttr idAttr = query.getDict().getIdAttr();
        boolean useSeq = (idAttr != null && idAttr.getFields().size() == 1 && DictRowAttr.isEmpty(data.getIdAttr()));
        String sql = sqlGen.getRowInsertSql(query, useSeq);

        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql,
                new String[] {query.getDict().getIdAttr().getFields().get(0).getName()})) {
            int paramCount = setParamsForInsert(query, data, ps) - 1;
            log.info("params: {}", paramCount);

            ps.execute();
            try(ResultSet rs = ps.getGeneratedKeys()) {
                if(rs.next()) {
                    rsToDictRowIdAttr(query, rs, data);
                } else {
                    throw new NsiDataException("not found");
                }
            }
            return get(connection, query, data.getIdAttr());
        } catch (SQLException e) {
            throw new NsiDataException("insert:" + e.getMessage(),e);
        }
    }

    private void updateRowData(DictRow row, DictRow data) throws SQLException {
        for (String attrName : row.getAttrs().keySet()){
            if (!data.getAttrs().containsKey(attrName)){
                data.setAttr(attrName, row.getAttr(attrName));
            }
        }
     }

    public DictRow update(Connection connection, NsiQuery query,
            DictRow data) {
        checkDictHasIdAttr(query.getDict());

        String sql = sqlGen.getRowUpdateSql(query);
        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql )) {
            if (query.getAttrs().size() != data.getAttrs().size()){
                DictRow row = get(connection, query, data.getIdAttr());
                updateRowData(row, data);
            }
            int paramCount = setParamsForUpdate(query, data, ps) - 1;
            log.info("params: {}", paramCount);

            int count = ps.executeUpdate();
            if(count == 0) {
                throw new NsiDataException("row not updated");
            } if(count > 1) {
                throw new NsiDataException(Joiner.on(" ").join("too many row updated:",count));
            }
            return get(connection, query, data.getIdAttr());
        } catch (SQLException e) {
            throw new NsiDataException("update:" + e.getMessage(),e);
        }
    }

    @Override
    public DictRow save(Connection connection, NsiQuery query, DictRow data,
            boolean insert) {
        checkDictHasIdAttr(query.getDict());

        DictRow result = null;
        if(insert) {
            result = insert(connection, query, data);
        } else {
            result = update(connection, query, data);
        }
        return result;
    }
    
    protected DictRow getSingleRow(final Connection conn, NsiQuery query, final BoolExp filter) {
		List<DictRow> rows = list(conn, query, filter, null, -1, -1, null, null);
		
		if(rows.size() == 0) {
			return null;
		} else if(rows.size() == 1) {
			return rows.get(0);
		} else {
			throw new NsiServiceException("Найдено " +rows.size()+ " строк в " +query.getDict().getName()+ " соответствующих условию %s "  +filter.toString());
		}
    }
    
    protected DictRow getByExternalAttrs(final Connection conn, final DictRow data) {
		NsiConfigDict dict = data.getDict();
		List<NsiConfigAttr> mAttrs = dict.getMergeExternalAttrs();
		Preconditions.checkArgument(mAttrs != null && mAttrs.size() > 0, "mergeExternalAttrs не заданы для %s", dict.getName());
		
		BoolExpBuilder fb = dict.filter().and().expList();
		if(dict.getOwnerAttr() != null && data.getOwnerAttr() != null) {
			fb.key(dict.getOwnerAttr().getName()).eq().value(data.getOwnerAttr()).add();
		}
		for(NsiConfigAttr configAttr : mAttrs) {
			DictRowAttr rowAttr = data.getAttr(configAttr.getName());
			Preconditions.checkNotNull(rowAttr, "Атрибут %s не существует в %s", configAttr.getName(), dict.getName()) ;
			fb.key(configAttr.getName()).eq().value(rowAttr).add();
		}
		BoolExp filter = fb.end().build();

		return getSingleRow(conn, dict.query().addAttrs(), filter);
    }
    
    @Override
	public DictRow mergeByExternalAttrs(Connection connection, DictRow data) {
    	NsiConfigDict dict = data.getDict();
    	
    	for(Map.Entry<String, DictRowAttr> entryAttr : data.getAttrs().entrySet()) {
    		NsiConfigAttr configAttr = dict.getAttr(entryAttr.getKey());
    		if(dict.isAttrHasRefAttrs(configAttr) && entryAttr.getValue().getValues() == null) {
    			Preconditions.checkNotNull(entryAttr.getValue().getRefAttrs(), "refAttrs не заданы для атрибута %s в %s", entryAttr.getKey(), dict.getName());
    			
    			List<NsiConfigAttr> mAttrs = configAttr.getRefDict().getMergeExternalAttrs();
    			Preconditions.checkArgument(mAttrs != null && mAttrs.size() > 0, "mergeExternalAttrs не заданы для %s", configAttr.getRefDict().getName());
    			
    			BoolExpBuilder fb = configAttr.getRefDict().filter().and().expList();
    			for(NsiConfigAttr mConfigAttr : mAttrs) {
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
    	
    	if(oldRow == null) {
    		data.builder().idAttrNull();
    		newRow = insert(connection, dict.query().addAttrs(data), data);
    	} else {
    		newRow = mergeDictRow(data, oldRow);
    		newRow = update(connection, dict.query().addAttrs(newRow), newRow);
    	}
    	
    	return newRow;
	}

	private String buildConditionString(Map<String, DictRowAttr> entry) {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, DictRowAttr> attr : entry.entrySet()) {
			sb.append(attr.getKey()).append("=");
			for(String value : attr.getValue().getValues()) {
				sb.append(value).append(",");
			}
			sb.append(" ");
		}
		
		return sb.toString();
	}

	private DictRow mergeDictRow(DictRow source, DictRow target) {
		NsiConfigAttr idAttr = target.getDict().getIdAttr();
		Preconditions.checkNotNull(idAttr, "Нет idAttr в %s", target.getDict().getName());
		
		for(Map.Entry<String, DictRowAttr> entry : source.getAttrs().entrySet()) {
			if(idAttr.getName().equals(entry.getKey())) {
				continue;
			}
			target.setAttr(entry.getKey(), entry.getValue());
		}
		
		return target;
	}
}
