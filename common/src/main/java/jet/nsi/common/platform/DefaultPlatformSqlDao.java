package jet.nsi.common.platform;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import jet.nsi.api.data.ConvertUtils;
import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.data.NsiQueryAttr;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.DictRowAttr;
import jet.nsi.api.model.MetaFieldType;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlDao;
import jet.nsi.common.data.NsiDataException;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

public abstract class DefaultPlatformSqlDao implements PlatformSqlDao {
    private static final Logger log = LoggerFactory.getLogger(DefaultPlatformSqlDao.class);


    protected final NsiPlatform nsiPlatform;
    protected final Settings settings;

    public DefaultPlatformSqlDao(NsiPlatform nsiPlatform) {
        this.nsiPlatform = nsiPlatform;
        this.settings = nsiPlatform.getJooqSettings();
    }

    @Override
    public String getDuplicateKeyViolationSqlState() {
        return "UNDEFINED_CODE";
    }

    @Override
    public String getForeignKeyViolationSqlState(){
        return "UNDEFINED_CODE";
    }

    @Override
    public boolean useUUIDForId() {
        return false;
    }

    @Override
    public int setParamsForUpdate(NsiQuery query, DictRow data, PreparedStatement ps) throws SQLException {
        return setParamsForUpdate(query, data, ps, true);
    }

    public int setParamsForUpdate(NsiQuery query, DictRow data, PreparedStatement ps, boolean skipIdInParams) throws SQLException {
        int index = 1;
        NsiConfigDict dict = query.getDict();
        NsiConfigAttr idAttr = dict.getIdAttr();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();

            List<NsiConfigField> fields = attr.getFields();

            String queryAttrName = attr.getName();
            DictRowAttr dataAttr = data.getAttrs().get(queryAttrName);
            // пропускаем id
            if (skipIdInParams && attr == idAttr) {
                continue;
            }

            if (dataAttr == null) {
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
        return index;
    }


    /**
     * Биндит к запросу значение поля под заданным индексом
     */
    @Override
    public void setParam(PreparedStatement ps, int index, NsiConfigField field,
                         String value) throws SQLException {
        setParam(ps, index, field.getType(), field.getSize(), field.getPrecision(), value);
    }

    @Override
    public void setParam(PreparedStatement ps, int index, MetaFieldType fieldType, int fieldSize,
                         String value) throws SQLException {
        setParam(ps, index, fieldType, fieldSize, 0, value);
    }

    @Override
    public void checkDataValues(List<NsiConfigField> fields,
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

    @Override
    public String getFieldSpelling(String field) {
        return field;
    }

    @Override
    public String getFieldValue(ResultSet rs, int index, NsiConfigField field)
            throws SQLException {
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
                return getStringFromClob(rs, index);
            default:
                throw new NsiDataException("unsupported field type: " + field.getType());
        }
    }

    @Override
    public String getStringFromClob(ResultSet rs, int index) throws SQLException {
        return getClobStringValue(rs.getClob(index));
    }

    protected String trimTrailing(String value) {
        return value == null ? null : CharMatcher.WHITESPACE.trimTrailingFrom(value);
    }

    @Override
    public String getClobStringValue(Clob clob) throws SQLException {
        return clob.getSubString(1, (int) clob.length());
    }

    @Override
    public void setParam(PreparedStatement ps, int index,
                         MetaFieldType fieldType, int fieldSize, int fieldPrecision,
                         Object value_) throws SQLException {
        // TODO: переписать это
        String value = (String) value_;

        switch (fieldType) {
            case BOOLEAN:
                setBooleanParam(ps, value, index);
                break;
            case NUMBER:
                if (Strings.isNullOrEmpty(value)) {
                    ps.setNull(index, Types.BIGINT);
                } else {
                    if (fieldPrecision > 0) {
                        ps.setBigDecimal(index, new BigDecimal(value));
                    } else if (fieldSize <= 19) {
                        ps.setLong(index, Long.parseLong(value));
                    } else {
                        ps.setBigDecimal(index, new BigDecimal(value));
                    }
                }
                break;
            case DATE_TIME:
                if (Strings.isNullOrEmpty(value)) {
                    ps.setNull(index, Types.DATE);
                } else {
                    DateTime dateTime = ConvertUtils.stringToDateTime(value);
                    ps.setTimestamp(index, new Timestamp(dateTime.getMillis()));
                }
                break;
            case VARCHAR:
                if (Strings.isNullOrEmpty(value)) {
                    ps.setNull(index, Types.VARCHAR);
                } else {
                    ps.setString(index, value);
                }
                break;
            case CHAR:
                if (Strings.isNullOrEmpty(value)) {
                    ps.setNull(index, Types.CHAR);
                } else {
                    ps.setString(index, Strings.padEnd(value, fieldSize, ' '));
                }
                break;
            case CLOB:
                setClobParam(ps, value, index);
                break;
            default:
                throw new NsiDataException(Joiner.on(" ").join("unsupported param type:", fieldType));
        }
    }

    @Override
    public void setBooleanParam(PreparedStatement ps, String value, int index) throws SQLException {
        if (Strings.isNullOrEmpty(value)) {
            ps.setNull(index, Types.BOOLEAN);
        } else {
            ps.setString(index, ConvertUtils.dbBoolToString(ConvertUtils.stringToBool(value)));
        }
    }

    @Override
    public void setClobParam(PreparedStatement ps, String value, int index) throws SQLException {
        if (Strings.isNullOrEmpty(value)) {
            ps.setNull(index, Types.CLOB);
        } else {
            Clob clob = ps.getConnection().createClob();
            clob.setString(1, value);
            ps.setClob(index, clob);
        }
    }

    @Override
    public long getCountFromRs(NsiQuery query, ResultSet rs)
            throws SQLException {
        return rs.getBigDecimal(1).longValue();
    }

    @Override
    public void executeSql(Connection connection, String sql) {
        log.info(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("execute: " + sql, e);
        }
    }

    @Override
    public DSLContext getQueryBuilder(Connection connection) {
        return DSL.using(connection, SQLDialect.DEFAULT, settings);
    }

    @Override
    public String wrapFilterFieldValue(BoolExp filter, NsiConfigField field,
                                       String val) {
        if (filter.getFunc().equals(OperationType.LIKE)) {
            return "%" + val + "%";
        } else if (filter.getFunc().equals(OperationType.CONTAINS)) {
            throw new NsiDataException("unsupported func: " + filter.getFunc());
        } else {
            return val;
        }
    }

    @Override
    public abstract int limit(PreparedStatement ps, int index, long offset, int size) throws SQLException;
}
