package jet.nsi.common.platform.postgresql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;

import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.common.platform.DefaultPlatformSqlGen;

public class PostgresqlPlatformSqlGen extends DefaultPlatformSqlGen {

    public PostgresqlPlatformSqlGen(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Condition getFieldFuncCondition(NsiQuery query, NsiConfigField field,
            BoolExp filter, SelectJoinStep<?> baseQuery) {
        switch (filter.getFunc()) {
        case OperationType.CONTAINS:
            Field<Object> f = field(NsiQuery.MAIN_ALIAS +"."+field.getName());
            return new PostgresqlTextSearchCondition(f);
        default:
            return super.getFieldFuncCondition(query, field, filter, baseQuery);
        }
        
    }
    
    @Override
    public Query limit(SelectJoinStep<?> baseQuery, long offset, int size) {
        if (size != -1) {
            List<SelectField<?>> limitFields = new ArrayList<>();
            limitFields.add(field("a.*"));
            limitFields.add(field("ROWNUM").as("rnum"));
            
            SelectConditionStep<Record> limitQuery = getQueryBuilder()
                    .select(limitFields)
                    .from(baseQuery.asTable("a"))
                    .where(field("ROWNUM").lessThan(val((Object)null)));
            if(offset != -1) {
                return getQueryBuilder()
                    .select(field("b.*"))
                    .from(limitQuery.asTable("b"))
                    .where(field("rnum").greaterOrEqual(val((Object)null)));
            } else {
                return limitQuery;
            }
        } else {
            return baseQuery;
        }
    }
}
