package jet.nsi.common.platform.postgresql;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.ConvertUtils;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.MetaFieldType;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.common.data.NsiDataException;
import jet.nsi.common.platform.DefaultPlatformSqlDao;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class PostgresqlPlatformSqlDao extends DefaultPlatformSqlDao {

    public PostgresqlPlatformSqlDao(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }

    @Override
    public String getFieldSpelling(String field) {
        return field != null ? field.toLowerCase() : null;
    }

    @Override
    public String wrapFilterFieldValue(BoolExp filter, NsiConfigField field,
            String val) {
        if (filter.getFunc().equals(OperationType.CONTAINS)) {
            // ** нужно использовать для обхода ограничения oracle
            // oracle11 не поддерживает left wildcard
            // https://docs.oracle.com/cd/B28359_01/text.111/b28304/csql.htm#i997256
            return "**" + replaceIllegalCharacters(val) + "*";
        } else {
            return super.wrapFilterFieldValue(filter, field, val);
        }
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
                return rs.getString(index);
            default:
                throw new NsiDataException("unsupported field type: " + field.getType());
        }
    }

    @Override
    public void setParam(PreparedStatement ps, int index,
                         MetaFieldType fieldType, int fieldSize, int fieldPrecision,
                         Object value_) throws SQLException {
        // TODO: переписать это
        String value = (String) value_;

        switch (fieldType) {
            case BOOLEAN:
                if (Strings.isNullOrEmpty(value)) {
                    ps.setNull(index, Types.VARCHAR);
                } else {
                    ps.setString(index, ConvertUtils.dbBoolToString(ConvertUtils.stringToBool(value)));
                }
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
                if (Strings.isNullOrEmpty(value)) {
                    ps.setNull(index, Types.VARCHAR);
                } else {
                    ps.setString(index, value);
                }
                break;
            default:
                throw new NsiDataException(Joiner.on(" ").join("unsupported param type:", fieldType));
        }
    }

    @Override
    public DataType<?> getDataType(MetaFieldType fieldType) {
        String type = null;
        switch (fieldType) {
        case BOOLEAN:
            type = "char";
            break;
        case DATE_TIME:
            type = "timestamp";
            break;
        case NUMBER:
            type = "numeric";
            break;
        case VARCHAR:
            type = "varchar";
            break;
        case CHAR:
            type = "char";
            break;
        case CLOB:
            type = "clob";
            break;
        default:
            throw new NsiServiceException("unsupported field type: " + fieldType);
        }
        return DefaultDataType.getDataType(SQLDialect.DEFAULT, type);
    }

    private String replaceIllegalCharacters(String value) {
        return value.replaceAll("[\"(),]", "");
    }
    
    @Override
    public DSLContext getQueryBuilder(Connection connection) {
        return DSL.using(connection,SQLDialect.POSTGRES_9_5,settings);
    }

    @Override
    public int limit(PreparedStatement ps, int index, long offset, int size) throws SQLException {
        if (size != -1){
            if(offset != -1) {
                ps.setLong(index++, size);
                ps.setLong(index++, offset);
            } else {
                ps.setLong(index++, size);
            }
        }
        return index;
    }

}
