package jet.nsi.common.platform;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.sequence;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.val;


import com.google.common.base.Strings;
import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiQueryAttr;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.SelectJoinStep;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlGen;
import jet.nsi.common.data.NsiDataException;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultPlatformSqlGen implements PlatformSqlGen {
    
    public static final String NEXTVAL = "nextval";
    public static final String CURRVAL = "currval";

    protected final NsiPlatform nsiPlatform;
    protected final Settings settings;
    
    public DefaultPlatformSqlGen(NsiPlatform nsiPlatform) {
        this.nsiPlatform = nsiPlatform;
        this.settings = nsiPlatform.getJooqSettings();
    }

    @Override
    public boolean isLockSupported() {
        return true;
    }

    @Override
    public String getRowInsertSql(NsiQuery query, boolean useSeq) {
        InsertSetStep<?> insertSetStep = getQueryBuilder().insertInto(table(query.getDict().getTable()));
        InsertSetMoreStep<?> insertSetMoreStep = null;
        NsiConfigDict dict = query.getDict();
        List<NsiConfigField> idFields = dict.getIdAttr().getFields();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            if(attr == query.getDict().getIdAttr() && useSeq) {
                if(attr.getFields().size() > 1) {
                    throw new NsiDataException("use seq possible for id attr with one field only");
                }
                // seq
                insertSetMoreStep = insertSetStep.set(field(idFields.get(0).getName()), sequenceFunсtion("seq_" + dict.getTable(), PlatformSqlGen.NEXTVAL));
            } else {
                for (NsiConfigField field : attr.getFields()) {
                    insertSetMoreStep = insertSetStep.set(field(field.getName(),String.class),val(""));
                }
            }
        }
        if(insertSetMoreStep != null) {
            if(idFields.size()==1) {
                insertSetMoreStep.returning(getReturningFields(query)).getSQL();
            }
            return insertSetMoreStep.getSQL();
        } else {
            throw new NsiDataException("no attrs found");
        }
    }

    protected List<Field<?>> getReturningFields(NsiQuery query) {
        List<NsiConfigField> fields = query.getDict().getIdAttr().getFields();
        List<Field<?>> result = new ArrayList<>(fields.size());
        for (NsiConfigField field : fields) {
            result.add(field(field.getName()));
        }
        return result;
    }

    @Override
    public String getRowUpdateSql(NsiQuery query) {
        NsiConfigDict dict = query.getDict();
        UpdateSetFirstStep<?> updateSetFirstStep = getQueryBuilder()
                .update(table(dict.getTable()));
        UpdateSetMoreStep<?> updateSetMoreStep = null;
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            if(attr != query.getDict().getIdAttr()) {
                for (NsiConfigField field : attr.getFields()) {
                    updateSetMoreStep  = updateSetFirstStep.set(
                            field(field.getName(),String.class),val(""));
                }
            }
        }
        if(updateSetMoreStep != null) {
            Condition condition = getIdCondition(query, "");
            return updateSetMoreStep.where(condition).getSQL();
        } else {
            throw new NsiDataException("no attrs found");
        }
    }


    protected Condition getIdCondition(NsiQuery query, String alias) {
        NsiConfigDict dict = query.getDict();

        Condition result = field( (Strings.isNullOrEmpty(alias) ? "" : alias + ".")
                + dict.getIdAttr().getFields().get(0).getName()).equal(val((Object) null));

        return result;
    }

    @Override
    public DSLContext getQueryBuilder() {
        DSLContext queryBuilder = DSL.using(nsiPlatform.getJooqSQLDialect(), settings);//todo
//        DSLContext queryBuilder = DSL.using(SQLDialect.DEFAULT, settings);
        return queryBuilder;
    }
    
    @Override
    public Settings getJooqSettings() {
        return settings;
    }

    @Override
    public Condition getFieldFuncCondition(NsiQuery query, NsiConfigField field,
            BoolExp filter, SelectJoinStep<?> baseQuery) {
        Field<Object> f;
        
        if(OperationType.LIKE.equals(filter.getFunc())) {
            f = field("lower(" + NsiQuery.MAIN_ALIAS +"."+field.getName() +")");
        } else {
            f = field(NsiQuery.MAIN_ALIAS +"."+field.getName());
        }
        
        switch (filter.getFunc()) {
        case OperationType.EQUALS:
            if (filter.getValue().getValues().get(0) != null){
                return f.eq(val((Object) null));
            }
            else{
                return f.isNull();
            }
        case OperationType.NOT_EQUALS:
            if (filter.getValue().getValues().get(0) != null){
                return f.notEqual(val((Object) null));
            }
            else{
                return f.isNotNull();
            }
        case OperationType.GT:
            return f.gt(val((Object) null));
        case OperationType.GE:
            return f.ge(val((Object) null));
        case OperationType.LT:
            return f.lt(val((Object) null));
        case OperationType.LE:
            return f.le(val((Object) null));
        case OperationType.LIKE:
            if (filter.getValue().getValues().get(0) != null){
                Field<String> value = null;
                return f.like(DSL.lower(value));
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
                return baseQuery.limit(val((int)offset), val(size));
            } else {
                return baseQuery.limit(val(size));
            }
        } else {
            return baseQuery;
        }
    }
    
    @Override
    public Object sequenceFunсtion(String name, String seqFunction) {
        switch (seqFunction) {
        case NEXTVAL:
            return field(sequence(name).nextval());
        case CURRVAL:
            return field(sequence(name).currval());
        default:
            throw new NsiDataException("Invalid function for using with sequence: " + seqFunction);
        }
    }
}
