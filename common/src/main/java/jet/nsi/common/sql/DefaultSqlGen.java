package jet.nsi.common.sql;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.sequence;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnStep;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.Table;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiConfigSourceQuery;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.data.NsiQueryAttr;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.MetaAttrType;
import jet.nsi.api.model.OperationType;
import jet.nsi.api.model.RefAttrsType;
import jet.nsi.api.model.SortExp;
import jet.nsi.api.platform.PlatformSqlGen;
import jet.nsi.api.sql.SqlGen;
import jet.nsi.common.data.NsiDataException;

public class DefaultSqlGen implements SqlGen {

    protected PlatformSqlGen platformSqlGen;
    
    public String getRowGetSql(NsiQuery query) {
        return getRowGetSql(query, false);
    }

    public String getRowGetSql(NsiQuery query, boolean lock) {
        return getRowGetSql(query, lock, RefAttrsType.REF_OBJECT_ATTRS);
    }

    public String getRowGetSql(NsiQuery query, boolean lock, RefAttrsType refAttrsType) {
        if(lock) {
            Collection<? extends SelectField<?>> selectFields = getSelectFields(query, false);
            Table<?> fromSource = createFromSource(query, null);
            SelectJoinStep<Record> baseQuery = getQueryBuilder().select(selectFields).from(fromSource);
            Condition condition = getIdCondition(query, baseQuery);
            return baseQuery.where(condition).forUpdate().getSQL();
        } else {
            SelectJoinStep<?> baseQuery = createBaseQuery(query, true, null, refAttrsType);
            Condition condition = getIdCondition(query, baseQuery);
            SelectConditionStep<?> resultQuery = baseQuery.where(condition);
            return resultQuery.getSQL();
        }
    }

    protected DSLContext getQueryBuilder() {
        return DSL.using(SQLDialect.DEFAULT, platformSqlGen.getJooqSettings());
    }

    protected SelectJoinStep<?> createBaseQuery(NsiQuery query, boolean includeRefFields) {
        return createBaseQuery(query, includeRefFields, null);
    }

    protected SelectJoinStep<?> createBaseQuery(NsiQuery query, boolean includeRefFields, String sourceQueryName) {
        return createBaseQuery(query, includeRefFields, sourceQueryName, RefAttrsType.REF_OBJECT_ATTRS);
    }

    protected SelectJoinStep<?> createBaseQuery(NsiQuery query, boolean includeRefFields, String sourceQueryName, RefAttrsType refAttrsType) {
        Collection<? extends SelectField<?>> selectFields = getSelectFields(query, true, refAttrsType);
        Table<?> fromSource = createFromSource(query, sourceQueryName);
        SelectJoinStep<Record> selectJoinStep = getQueryBuilder().select(selectFields).from(fromSource);

        return addRefAttrJoins(query, selectJoinStep);
    }

    private Table<?> createFromSource(NsiQuery query, String sourceQueryName) {
        // если запрос не задан и нет таблицы, используем запрос по умолчанию
        if( query.getDict().getTable() == null && sourceQueryName == null) {
            sourceQueryName = NsiQuery.MAIN_QUERY;
        }

        Table<?> fromSource;
        NsiConfigDict dict = query.getDict();
        if(sourceQueryName == null) {
            fromSource = table(dict.getTable());
        } else {
            fromSource = table("( " + dict.getSourceQuery(sourceQueryName).getSql() + " ) ");
        }
        return fromSource.as(NsiQuery.MAIN_ALIAS);
    }

    protected Condition getIdCondition(NsiQuery query, SelectJoinStep<?> baseQueryPart) {
        return getIdCondition(query, NsiQuery.MAIN_ALIAS);
    }

    protected Condition getIdCondition(NsiQuery query, String alias) {
        NsiConfigDict dict = query.getDict();

        Condition result = field( (Strings.isNullOrEmpty(alias) ? "" : alias + ".")
                + dict.getIdAttr().getFields().get(0).getName()).equal(val((Object) null));

        return result;
    }

    protected SelectJoinStep<?> addRefAttrJoins(NsiQuery query, SelectJoinStep<?> selectJoinStep) {
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            // включаем поля RefObject атрибутов
            if(attr.getType()==MetaAttrType.REF) {
                SelectOnStep<?> beforeOn;
                NsiConfigDict refDict = attr.getRefDict();
                if(refDict.getTable() != null) {
                    beforeOn = selectJoinStep.leftOuterJoin(table(refDict.getTable()).as(queryAttr.getRefAlias()));
                } else {
                    NsiConfigSourceQuery defaultQuery = refDict.getSourceQuery(NsiQuery.MAIN_QUERY);
                    Preconditions.checkNotNull(defaultQuery);
                    beforeOn = selectJoinStep.leftOuterJoin(table("( " + defaultQuery.getSql() + " ) ").as(queryAttr.getRefAlias()));
                }
                NsiConfigAttr refIdAttr = refDict.getIdAttr();

                Condition cond = createJoinFieldCondition(queryAttr, attr.getFields().get(0), refIdAttr.getFields().get(0));

                for(int i=1;i<attr.getFields().size();i++) {
                    NsiConfigField attrField = attr.getFields().get(i);
                    NsiConfigField refIdField = refIdAttr.getFields().get(i);
                    cond.and(createJoinFieldCondition(queryAttr, attrField, refIdField));
                }
                beforeOn.on(cond);
            }
        }
        return selectJoinStep;
    }

    protected Condition createJoinFieldCondition(NsiQueryAttr queryAttr,
            NsiConfigField refAttrField, NsiConfigField refIdField) {
        Condition condition = field(queryAttr.getAlias() + "." + refAttrField.getName())
                .eq(field(queryAttr.getRefAlias() + "." + refIdField.getName()));
        return condition;
    }

    protected List<SelectField<?>> getSelectFields(NsiQuery query, boolean includeRefFields) {
        return getSelectFields(query, includeRefFields, RefAttrsType.REF_OBJECT_ATTRS);
    }

    protected List<SelectField<?>> getSelectFields(NsiQuery query, boolean includeRefFields, RefAttrsType refAttrsType) {
        List<SelectField<?>> result = new ArrayList<>();
        NsiConfigDict dict = query.getDict();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            // включаем поля атрибутов
            for (NsiConfigField field : attr.getFields()) {
                result.add(field(queryAttr.getAlias() + "." + field.getName()));
            }
            if(includeRefFields && dict.isAttrHasRefAttrs(attr)) {
                List<NsiConfigAttr> refAttrs = attr.getRefDict().getRefAttrs(refAttrsType);
                if(refAttrs != null) {
                    for (NsiConfigAttr refAttr : refAttrs) {
                        for (NsiConfigField field : refAttr.getFields()) {
                            String alias = queryAttr.getRefAlias() + "_" + field.getName();
                            result.add(field(queryAttr.getRefAlias() + "." + field.getName()).as(alias));
                        }
                    }
                }
            }
        }
        return result;
    }

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
                insertSetMoreStep = insertSetStep.set(field(idFields.get(0).getName()), platformSqlGen.sequenceFunсtion("seq_" + dict.getTable(), PlatformSqlGen.NEXTVAL));
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

    @Override
    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size) {
        return getListSql(query, filter, sortList, offset, size, null);
    }

    @Override
    public String getListSql(NsiQuery query, BoolExp filter,
            List<SortExp> sortList, long offset, int size, String sourceQuery) {
        return getListSql(query, filter, sortList, offset, size, sourceQuery, RefAttrsType.REF_OBJECT_ATTRS);
    }

    @Override
    public String getListSql(NsiQuery query, BoolExp filter,
            List<SortExp> sortList, long offset, int size, String sourceQuery, RefAttrsType refAttrsType) {
        checkPaginationExp(offset, size);

        SelectJoinStep<?> baseQuery = createBaseQuery(query, true, sourceQuery, refAttrsType);
        Condition filterCondition = getWhereCondition(query, filter, baseQuery);
        if(filterCondition != null) {
            baseQuery.where(filterCondition);
        }

        Collection<? extends SortField<?>> sortFields = getSortFields(query, sortList);
        if(sortFields != null) {
            baseQuery.orderBy(sortFields);
        }

        if( size != -1 ) {
            return platformSqlGen.limit(baseQuery, offset, size).getSQL();
        } else {
            return baseQuery.getSQL();
        }
        
    }

    protected void checkPaginationExp(long offset, int size) {
        if ((offset == -1 && size == -1) || (offset != -1 && size != -1)) {
            return;
        }
        throw new NsiDataException("invalid condition [offset : "
                + offset + ", size : " + size + "]");
    }

    protected Condition getWhereCondition(NsiQuery query, BoolExp filter, SelectJoinStep<?> baseQuery) {
        Condition filterCondition = getFilterCondition(query, filter, baseQuery);
        return filterCondition;
    }

    protected Collection<? extends SortField<?>> getSortFields(NsiQuery query, List<SortExp> sortList) {
        if(sortList == null) {
            return null;
        }
        List<SortField<?>> result = new ArrayList<>();
        for (SortExp sortExp : sortList) {
            NsiQueryAttr sortAttr = query.getAttr(sortExp.getKey());
            SortOrder sortOrder = sortExp.getAsc() == Boolean.TRUE ? SortOrder.ASC : SortOrder.DESC;
            for (NsiConfigField field : sortAttr.getAttr().getFields()) {
                result.add(field(sortAttr.getAlias() + "." + field.getName()).sort(sortOrder));
            }
        }
        return result;
    }

    protected Condition getFilterCondition(NsiQuery query, BoolExp filter, SelectJoinStep<?> baseQuery) {
        if(filter == null) {
            return null;
        }
        switch (filter.getFunc()) {
        case OperationType.AND:
            return getAndCondition(query, filter.getExpList(), baseQuery);
        case OperationType.OR:
            return getOrCondition(query, filter.getExpList(), baseQuery);
        case OperationType.NOTAND:
            return getAndCondition(query, filter.getExpList(), baseQuery).not();
        case OperationType.NOTOR:
            return getOrCondition(query, filter.getExpList(), baseQuery).not();
        case OperationType.EQUALS:
        case OperationType.NOT_EQUALS:
        case OperationType.LIKE:
        case OperationType.CONTAINS:
        case OperationType.GT:
        case OperationType.GE:
        case OperationType.LT:
        case OperationType.LE:
        case OperationType.NOTNULL:
            return getFuncCondition(query, filter, baseQuery);
        default:
            throw new NsiDataException("invalid func: " + filter.getFunc());
        }
    }

    protected Condition getFuncCondition(NsiQuery query, BoolExp filter, SelectJoinStep<?> baseQuery) {
        NsiConfigAttr configAttr = query.getDict().getAttr(filter.getKey());
        List<NsiConfigField> fields = configAttr.getFields();
        Condition condition = getFieldFuncCondition(query, fields.get(0), filter, baseQuery);
        for(int i=1;i<fields.size();i++) {
            condition = condition.and(getFieldFuncCondition(query, fields.get(i), filter, baseQuery));
        }
        return condition;
    }

    protected Condition getFieldFuncCondition(NsiQuery query,
            NsiConfigField field, BoolExp filter, SelectJoinStep<?> baseQuery) {
        return platformSqlGen.getFieldFuncCondition(query, field, filter, baseQuery);
    }

    protected void checkExpList(List<BoolExp> expList) {
        if(expList == null || expList.size() == 0) {
            throw new NsiDataException("empty exp list");
        }
    }

    protected Condition getOrCondition(NsiQuery query, List<BoolExp> expList, SelectJoinStep<?> baseQuery) {
        checkExpList(expList);
        Condition condition = getFilterCondition(query, expList.get(0), baseQuery);
        for(int i=1;i<expList.size();i++) {
            Condition c = getFilterCondition(query, expList.get(i), baseQuery);
            if(c != null) {
                condition = condition.or(c);
            }
        }
        return condition;
    }

    protected Condition getAndCondition(NsiQuery query, List<BoolExp> expList, SelectJoinStep<?> baseQuery) {
        checkExpList(expList);
        Condition condition = getFilterCondition(query, expList.get(0), baseQuery);
        for(int i=1;i<expList.size();i++) {
            Condition c = getFilterCondition(query, expList.get(i), baseQuery);
            if(c != null) {
                condition = condition.and(c);
            }
        }
        return condition;
    }

    @Override
    public String getCountSql(NsiQuery query, BoolExp filter) {
        return getCountSql(query, filter, null);
    }

    @Override
    public String getCountSql(NsiQuery query, BoolExp filter, String sourceQueryName) {
        DSLContext queryBuilder = getQueryBuilder();

        Table<?> fromSource = createFromSource(query, sourceQueryName);

        SelectJoinStep<Record1<Integer>> selectQueryPart = queryBuilder.select(count())
            .from(fromSource);

        SelectJoinStep<?> baseQueryPart = addRefAttrJoins(query, selectQueryPart);

        Condition filterCondition = getWhereCondition(query, filter, baseQueryPart);

        if(filterCondition != null) {
            baseQueryPart.where(filterCondition);
        }

        return baseQueryPart.getSQL();
    }

    public void setPlatformSqlGen(PlatformSqlGen platformSqlGen) {
        this.platformSqlGen = platformSqlGen;
    }


}
