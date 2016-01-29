package jet.isur.nsi.common.platform;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.SelectJoinStep;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.OperationType;
import jet.isur.nsi.api.platform.NsiPlatform;
import jet.isur.nsi.api.platform.PlatformSqlGen;
import jet.isur.nsi.common.data.NsiDataException;

public class DefaultPlatformSqlGen implements PlatformSqlGen {

    protected final NsiPlatform nsiPlatform;
    protected final Settings settings;
    
    public DefaultPlatformSqlGen(NsiPlatform nsiPlatform) {
        this.nsiPlatform = nsiPlatform;
        this.settings = nsiPlatform.getJooqSettings();
    }

    @Override
    public DSLContext getQueryBuilder() {
        DSLContext queryBuilder = DSL.using(SQLDialect.DEFAULT,settings );
        return queryBuilder;
    }

    @Override
    public Condition getFieldFuncCondition(NsiQuery query, NsiConfigField field,
            BoolExp filter, SelectJoinStep<?> baseQuery) {
        Field<Object> f = field(NsiQuery.MAIN_ALIAS +"."+field.getName());
        switch (filter.getFunc()) {
        case OperationType.EQUALS:
            if (filter.getValue().getValues().get(0) != null){
                return f.eq(val(null));
            }
            else{
                return f.isNull();
            }
        case OperationType.NOT_EQUALS:
            if (filter.getValue().getValues().get(0) != null){
                return f.notEqual(val(null));
            }
            else{
                return f.isNotNull();
            }
        case OperationType.GT:
            return f.gt(val(null));
        case OperationType.GE:
            return f.ge(val(null));
        case OperationType.LT:
            return f.lt(val(null));
        case OperationType.LE:
            return f.le(val(null));
        case OperationType.LIKE:
            if (filter.getValue().getValues().get(0) != null){
                Field<String> value = null;
                return f.like(value );
            }
            else{
                return f.isNull();
            }
        case OperationType.CONTAINS:
            throw new NsiDataException("unsupported func: " + filter.getFunc());
        case OperationType.NOTNULL:
            return f.isNotNull();
        default:
            throw new NsiDataException("invalid func: " + filter.getFunc());
        }
    }

    @Override
    public Query limit(SelectJoinStep<?> baseQuery, long offset, int size) {
        if(size != -1) {
            if(offset != -1) {
                return baseQuery.limit(DSL.val((int)offset), DSL.val(size));
            } else {
                return baseQuery.limit(DSL.val(size));
            }
        } else {
            return baseQuery;
        }
    }

}
