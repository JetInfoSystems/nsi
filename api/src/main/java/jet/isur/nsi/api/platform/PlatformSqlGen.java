package jet.isur.nsi.api.platform;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SelectJoinStep;

import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;

public interface PlatformSqlGen {

    DSLContext getQueryBuilder();

    Condition getFieldFuncCondition(NsiQuery query,
            NsiConfigField field, BoolExp filter, SelectJoinStep<?> baseQuery);

    Query limit(SelectJoinStep<?> baseQuery, long offset, int size);

}
