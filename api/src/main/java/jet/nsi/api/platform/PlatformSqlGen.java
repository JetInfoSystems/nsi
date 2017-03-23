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

    String getRowDeleteSql(NsiQuery query, BoolExp filter);

    String getRowUpdateSql(NsiQuery query, BoolExp filter);

    Condition getWhereCondition(NsiQuery query, BoolExp filter, String alias);
    Condition getWhereCondition(NsiQuery query, BoolExp filter);

    DSLContext getQueryBuilder();
    Settings getJooqSettings();


    Condition getFieldFuncCondition(NsiConfigField field, BoolExp filter, String alias);

    Query limit(SelectJoinStep<?> baseQuery, long offset, int size);

    Object sequenceFunсtion(String name, String seqFunction);

    boolean isLockSupported();

}
