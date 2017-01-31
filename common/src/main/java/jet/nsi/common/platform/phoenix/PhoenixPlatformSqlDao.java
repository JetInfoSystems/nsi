package jet.nsi.common.platform.phoenix;

import com.google.common.base.Strings;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.ConvertUtils;
import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.MetaFieldType;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.common.platform.DefaultPlatformSqlDao;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PhoenixPlatformSqlDao extends DefaultPlatformSqlDao {

    public PhoenixPlatformSqlDao(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }

    @Override
    public boolean useUUIDForId() {
        return true;
    }

//    @Override
//    public String getFieldSpelling(String field) {
//        return field != null ? field.toLowerCase() : null;
//    }

    @Override
    public int setParamsForUpdate(NsiQuery query, DictRow data, PreparedStatement ps) throws SQLException {
        return setParamsForUpdate(query, data, ps, false);
    }


    @Override
    public String getStringFromClob(ResultSet rs, int index) throws SQLException {
        return rs.getString(index);
    }


    @Override
    public void setBooleanParam(PreparedStatement ps, String value, int index) throws SQLException {
        if(Strings.isNullOrEmpty(value)) {
            ps.setNull(index, Types.BOOLEAN);
        } else {
            ps.setBoolean(index, Boolean.valueOf(value));
        }
    }

    @Override
    public void setClobParam(PreparedStatement ps, String value, int index) throws SQLException {
        if (Strings.isNullOrEmpty(value)) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    @Override
    public DataType<?> getDataType(MetaFieldType fieldType) {
        String type;
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
