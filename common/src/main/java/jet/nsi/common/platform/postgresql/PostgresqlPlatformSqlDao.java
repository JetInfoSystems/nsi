package jet.nsi.common.platform.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;

import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.MetaFieldType;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.common.platform.DefaultPlatformSqlDao;

public class PostgresqlPlatformSqlDao extends DefaultPlatformSqlDao {

    public PostgresqlPlatformSqlDao(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
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
    public DataType<?> getDataType(MetaFieldType fieldType) {
        String type = null;
        switch (fieldType) {
        case BOOLEAN:
            type = "char";
            break;
        case DATE_TIME:
            type = "date";
            break;
        case NUMBER:
            type = "number";
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
