package jet.nsi.common.platform.postgresql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.sequence;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.InsertSetStep;
import org.jooq.Query;
import org.jooq.SelectJoinStep;

import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.common.data.NsiDataException;
import jet.nsi.common.platform.DefaultPlatformSqlGen;

public class PostgresqlPlatformSqlGen extends DefaultPlatformSqlGen {

    public PostgresqlPlatformSqlGen(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Condition getFieldFuncCondition(NsiConfigField field, BoolExp filter, String alias) {
        switch (filter.getFunc()) {
        case OperationType.CONTAINS:
            Field<Object> f = field(NsiQuery.MAIN_ALIAS +"."+field.getName());
            return new PostgresqlTextSearchCondition(f);
        default:
            return super.getFieldFuncCondition(field, filter, alias);
        }
    }
    
    
    @Override
    public Object sequenceFunсtion(String name, String seqFunction) {
        switch (seqFunction) {
        case NEXTVAL:
            return field(NEXTVAL + "('" + sequence(name) + "')");
        case CURRVAL:
            return field(CURRVAL + "('" + sequence(name) + "')");
        default:
            throw new NsiDataException("Invalid function for using with sequence: " + seqFunction);
        }
    }
}
