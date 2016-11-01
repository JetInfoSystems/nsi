package jet.nsi.common.platform.postgresql;

import static org.jooq.impl.DSL.field;

import org.jooq.Condition;
import org.jooq.Field;
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
}
