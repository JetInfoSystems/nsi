package jet.isur.nsi.common.sql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.data.BoolExpVisitor;
import jet.isur.nsi.api.data.ConvertUtils;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.OperationType;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.sql.SqlDao;
import jet.isur.nsi.api.sql.SqlGen;
import jet.isur.nsi.common.data.NsiDataException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

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
                    NsiQueryAttr queryAttr = query.getAttr(filter.getKey());
                    NsiConfigAttr attr = queryAttr.getAttr();
                    String queryAttrName = attr.getName();

                    List<NsiConfigField> fields = attr.getFields();

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
        Map<String, DictRowAttr> resultAttrs = new HashMap<String, DictRowAttr>(query.getAttrs().size());
        result.setAttrs(resultAttrs);
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            DictRowAttr attrValue = new DictRowAttr();
            resultAttrs.put(attr.getName(), attrValue);
            ArrayList<String> fieldValues = new ArrayList<>(queryAttr.getAttr().getFields().size());
            attrValue.setValues(fieldValues);
            for (NsiConfigField field : attr.getFields()) {
                fieldValues.add(getFieldValue(rs,index,field));
                index++;
            }
            if(attr.getType()==MetaAttrType.REF) {
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

        default:
            throw new NsiDataException("unsupported field type: " + field.getType());
        }
    }

    protected String trimTrailing(String value) {
        return value == null ? null : CharMatcher.WHITESPACE.trimTrailingFrom(value);
    }

    public void setParamsForInsert(NsiQuery query, DictRow data,
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

            if(dataAttr == null) {
                throw new NsiDataException(
                        "can't find data attr for query attr: " + queryAttrName);
            }
            List<String> dataValues = dataAttr.getValues();
            checkDataValues(fields, queryAttrName, dataValues);

            // если у нас вставка и ид атрибут состоит из одного поля и имеет null значение
            // пропускаем его, потому что он получается из последовательности
            if (attr == idAttr
                    && idAttr.getFields().size() == 1
                    && dataValues.get(0) == null) {
                continue;
            }

            int i = 0;
            for (NsiConfigField field : fields) {
                setParam(ps, index, field, dataValues.get(i));
                index++;
                i++;
            }
        }
    }

    public void setParamsForUpdate(NsiQuery query, DictRow data,
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
    }

    public int setParamsForList(NsiQuery query, PreparedStatement ps, BoolExp filter, long offset, int size) throws SQLException {
       	int index = setParamsForFilter(query, ps, 1, filter);
       	if (offset != -1 && size != -1){
       		ps.setLong(index++, offset+size+1);
       		ps.setLong(index++, offset+1);
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
        DictRow result = new DictRow();
        String sql = sqlGen.getRowGetSql(query);
        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            setParamsForGetWhere(query, ps, id);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    rsToDictRow(query, rs, result);
                } else {
                    throw new NsiDataException(Joiner.on(" ").join("not foud", id));
                }
            }
        } catch (Exception e) {
            throw new NsiDataException("get",e);
        }
        return result;
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
        switch (field.getType()) {
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
                ps.setLong(index, Long.parseLong(value));
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
                ps.setString(index, Strings.padEnd(value, field.getSize(), ' '));
            }
            break;
        default:
            throw new NsiDataException(Joiner.on(" ").join("unsupported param type:",field.getType()));
        }
    }

    public List<DictRow> list(Connection connection, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size) {
        List<DictRow> result = new ArrayList<>();
        String sql = sqlGen.getListSql(query, filter, sortList, offset, size);
        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            setParamsForList(query, ps, filter, offset, size);
            if(ps.execute()) {
                try(ResultSet rs = ps.getResultSet()) {
                    while(rs.next()) {
                        DictRow data = new DictRow();
                        rsToDictRow(query, rs, data);
                        result.add(data);
                    }
                }
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new NsiDataException("list",e);
        }
        return result;
    }

    public long count(Connection connection, NsiQuery query, BoolExp filter) {
        String sql = sqlGen.getCountSql(query, filter);
        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql )) {
            setParamsForFilter(query, ps, 1, filter);
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
            throw new NsiDataException("count",e);
        }
    }

    public DictRow insert(Connection connection, NsiQuery query, DictRow data) {
        NsiConfigAttr idAttr = query.getDict().getIdAttr();
        DictRowAttr idAttrValue = data.getAttrs().get(idAttr.getName());
        String sql = sqlGen.getRowInsertSql(query, idAttrValue != null
                && idAttrValue.getValues().size() == 1
                && idAttrValue.getValues().get(0) == null);

        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql,
                new String[] {query.getDict().getIdAttr().getFields().get(0).getName()})) {
            setParamsForInsert(query, data, ps);
            ps.execute();
            try(ResultSet rs = ps.getGeneratedKeys()) {
                if(rs.next()) {
                    rsToDictRowIdAttr(query, rs, data);
                } else {
                    throw new NsiDataException("not found");
                }
            }
            return get(connection, query, new DictRowBuilder(query, data).getIdAttr());
        } catch (SQLException e) {
            throw new NsiDataException("insert",e);
        }
    }

	private void updateRowData(DictRow row, DictRow data) throws SQLException {
		for (String field : row.getAttrs().keySet()){
			if (!data.getAttrs().containsKey(field)){
				data.getAttrs().put(field, row.getAttrs().get(field));
			}
		}
 	}
	
    public DictRow update(Connection connection, NsiQuery query,
            DictRow data) {
    	String sql = sqlGen.getRowUpdateSql(query);
        log.info(sql);
        try(PreparedStatement ps = connection.prepareStatement(sql )) {
        	if (query.getAttrs().size() != data.getAttrs().size()){
        		DictRow row = get(connection, query, data.getAttrs()
        				.get(query.getDict().getIdAttr().getName()));
        		updateRowData(row, data);
        	}
            setParamsForUpdate(query, data, ps);
            int count = ps.executeUpdate();
            if(count == 0) {
                throw new NsiDataException("row not updated");
            } if(count > 1) {
                throw new NsiDataException(Joiner.on(" ").join("too many row updated:",count));
            }
            return get(connection, query, new DictRowBuilder(query, data).getIdAttr());
        } catch (SQLException e) {
            throw new NsiDataException("update",e);
        }
    }

    @Override
    public DictRow save(Connection connection, NsiQuery query, DictRow data,
            boolean insert) {
        DictRow result = null;
        if(insert) {
        	result = insert(connection, query, data);
        } else {
        	result = update(connection, query, data);
        }
        return result;
    }

}
