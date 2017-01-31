package jet.nsi.api.platform;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SelectJoinStep;
import org.jooq.conf.Settings;

import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;

public interface PlatformSqlGen {
    public static final String NEXTVAL = "nextval";
    public static final String CURRVAL = "currval";

    String getRowInsertSql(NsiQuery query, boolean useSeq);

    String getRowUpdateSql(NsiQuery query);

    DSLContext getQueryBuilder();
    Settings getJooqSettings();

    Condition getFieldFuncCondition(NsiQuery query,
            NsiConfigField field, BoolExp filter, SelectJoinStep<?> baseQuery);

    Query limit(SelectJoinStep<?> baseQuery, long offset, int size);

    Object sequenceFunсtion(String name, String seqFunction);

    boolean isLockSupported();

}
