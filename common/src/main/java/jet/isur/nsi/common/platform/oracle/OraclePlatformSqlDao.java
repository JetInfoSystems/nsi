package jet.isur.nsi.common.platform.oracle;

import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDataType;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.OperationType;
import jet.isur.nsi.api.platform.NsiPlatform;
import jet.isur.nsi.common.platform.DefaultPlatformSqlDao;

public class OraclePlatformSqlDao extends DefaultPlatformSqlDao {

    
    public OraclePlatformSqlDao(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }

    @Override
    public String wrapFilterFieldValue(BoolExp filter, NsiConfigField field,
            String val) {
        if (filter.getFunc().equals(OperationType.CONTAINS)) {
            // ** нужно использовать для обхода ограничения oracle
            // oracle11 не поддерживает left wildcard
            // https://docs.oracle.com/cd/B28359_01/text.111/b28304/csql.htm#i997256
            return "**" + val + "*";
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
            type = "varchar2";
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

}
