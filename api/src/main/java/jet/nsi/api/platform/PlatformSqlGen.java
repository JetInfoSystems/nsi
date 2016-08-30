package jet.nsi.api.platform;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SelectJoinStep;

import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;

public interface PlatformSqlGen {

    DSLContext getQueryBuilder();

    Condition getFieldFuncCondition(NsiQuery query,
            NsiConfigField field, BoolExp filter, SelectJoinStep<?> baseQuery);

    Query limit(SelectJoinStep<?> baseQuery, long offset, int size);

}
