package jet.isur.nsi.common.sql;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.sequence;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.common.data.NsiDataException;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnStep;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

public class DefaultSqlGen {

    private static Settings settings = new Settings();
    static {
        settings.setRenderNameStyle(RenderNameStyle.AS_IS);
    }

    public String getRowGetSql(NsiQuery query) {
        return createBaseQuery(query, true).where(getIdCondition(query)).getSQL();
    }

    protected DSLContext getQueryBuilder() {
        DSLContext queryBuilder = DSL.using(SQLDialect.DEFAULT,settings );
        return queryBuilder;
    }

    protected SelectJoinStep<?> createBaseQuery(NsiQuery query, boolean includeRefFields) {
        Collection<? extends SelectField<?>> selectFields = getSelectFields(query, true);
        SelectJoinStep<Record> selectJoinStep = getQueryBuilder().select(selectFields)
            .from(table(query.getDict().getTable()).as(NsiQuery.MAIN_ALIAS));

        return addRefAttrJoins(query, selectJoinStep);
    }


    protected Condition getIdCondition(NsiQuery query) {
        NsiConfigAttr idAttr = query.getDict().getIdAttr();

        Condition result = field(NsiQuery.MAIN_ALIAS + "." + idAttr.getFields().get(0).getName()).equal(val(null));
        for ( NsiConfigField field : idAttr.getFields()) {
            result.and(field(NsiQuery.MAIN_ALIAS + "." + field.getName()).equal(val(null)));
        }
        return result;
    }

    protected SelectJoinStep<?> addRefAttrJoins(NsiQuery query, SelectJoinStep<?> selectJoinStep) {
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            // включаем поля RefObject атрибутов
            if(attr.getType()==MetaAttrType.REF) {
                SelectOnStep<?> beforeOn;
                if(attr.isRequired()) {
                     beforeOn = selectJoinStep.join(table(attr.getRefDict().getTable()).as(queryAttr.getRefAlias()));
                } else {
                    beforeOn = selectJoinStep.leftOuterJoin(table(attr.getRefDict().getTable()).as(queryAttr.getRefAlias()));
                }
                NsiConfigAttr refIdAttr = attr.getRefDict().getIdAttr();

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
        List<SelectField<?>> result = new ArrayList<>();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            // включаем поля атрибутов
            for (NsiConfigField field : queryAttr.getAttr().getFields()) {
                result.add(field(queryAttr.getAlias() + "." + field.getName()));
            }
            // включаем поля RefObject атрибутов
            if(includeRefFields && queryAttr.getAttr().getType()==MetaAttrType.REF) {
                for (NsiConfigAttr refAttr : queryAttr.getAttr().getRefDict().getRefObjectAttrs()) {
                    for (NsiConfigField field : refAttr.getFields()) {
                        result.add(field(queryAttr.getRefAlias() + "." + field.getName()));
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
                insertSetMoreStep = insertSetStep.set(field(idFields.get(0).getName()),sequence( "seq_" + dict.getTable()).nextval());
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
        UpdateSetFirstStep<?> updateSetFirstStep = getQueryBuilder()
                .update(table(query.getDict().getTable()).as(NsiQuery.MAIN_ALIAS));
        UpdateSetMoreStep<?> updateSetMoreStep = null;
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            if(attr != query.getDict().getIdAttr()) {
                for (NsiConfigField field : attr.getFields()) {
                   updateSetMoreStep  = updateSetFirstStep.set(
                           field(queryAttr.getAlias() + "." + field.getName(),String.class),val(""));
                }
            }
        }
        if(updateSetMoreStep != null) {
            return updateSetMoreStep.where(getIdCondition(query)).getSQL();
        } else {
            throw new NsiDataException("no attrs found");
        }
    }

    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size) {
        SelectJoinStep<?> baseQueryPart = createBaseQuery(query, true);
        Condition filterCondition = getFilterCondition(query, filter);
        if(filterCondition != null) {
            baseQueryPart.where(filterCondition);
        }

        Collection<? extends SortField<?>> sortFields = getSortFields(query, sortList);
        if(sortFields != null) {
            baseQueryPart.orderBy(sortFields);
        }

        if(size != -1) {
            baseQueryPart.limit(size);
        }

        return baseQueryPart.getSQL();
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

    protected Condition getFilterCondition(NsiQuery query, BoolExp filter) {
        if(filter == null) {
            return null;
        }
        switch (filter.getFunc()) {
        case "and":
            return getAndCondition(query, filter.getExpList());
        case "or":
            return getOrCondition(query, filter.getExpList());
        case "notAnd":
            return getAndCondition(query, filter.getExpList()).not();
        case "notOr":
            return getOrCondition(query, filter.getExpList()).not();
        case "=":
            return getFuncCondition(query, filter);
        default:
            throw new NsiDataException("invalid func: " + filter.getFunc());
        }
    }

    protected Condition getFuncCondition(NsiQuery query, BoolExp filter) {
        NsiQueryAttr filterAttr = query.getAttr(filter.getKey());
        List<NsiConfigField> fields = filterAttr.getAttr().getFields();
        Condition condition = getFieldFuncCondition(query, fields.get(0),filter);
        for(int i=1;i<fields.size();i++) {
            condition = condition.and(getFieldFuncCondition(query, fields.get(i),filter));
        }
        return condition;
    }

    protected Condition getFieldFuncCondition(NsiQuery query,
            NsiConfigField field, BoolExp filter) {
        switch (filter.getFunc()) {
        case "=":
            if (filter.getValue().getValues().get(0) != null){
                return field(NsiQuery.MAIN_ALIAS +"."+field.getName()).eq(val(null));
            }
            else{
                return field(NsiQuery.MAIN_ALIAS +"."+field.getName()).isNull();
            }
        default:
            throw new NsiDataException("invalid func: " + filter.getFunc());
        }
    }

    protected Condition getOrCondition(NsiQuery query, List<BoolExp> expList) {
        checkExpList(expList);
        Condition condition = getFilterCondition(query, expList.get(0));
        for(int i=1;i<expList.size();i++) {
            condition = condition.or(getFilterCondition(query, expList.get(i)));
        }
        return condition;
    }

    protected Condition getAndCondition(NsiQuery query, List<BoolExp> expList) {
        checkExpList(expList);
        Condition condition = getFilterCondition(query, expList.get(0));
        for(int i=1;i<expList.size();i++) {
            condition = condition.and(getFilterCondition(query, expList.get(i)));
        }
        return condition;
    }

    protected void checkExpList(List<BoolExp> expList) {
        if(expList == null || expList.size() == 0) {
            throw new NsiDataException("empty exp list");
        }
    }

    public String getCountSql(NsiQuery query, BoolExp filter) {
        DSLContext queryBuilder = getQueryBuilder();

        SelectJoinStep<Record1<Integer>> selectQueryPart = queryBuilder.select(count())
            .from(table(query.getDict().getTable()).as(NsiQuery.MAIN_ALIAS));

        SelectJoinStep<?> baseQueryPart = addRefAttrJoins(query, selectQueryPart);

        Condition filterCondition = getFilterCondition(query, filter);

        if(filterCondition != null) {
            baseQueryPart.where(filterCondition);
        }

        return baseQueryPart.getSQL();
    }

}
