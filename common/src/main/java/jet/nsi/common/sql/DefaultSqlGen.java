package jet.nsi.common.sql;

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
import org.jooq.Condition;
import org.jooq.DSLContext;
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
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.val;

public class DefaultSqlGen implements SqlGen {

    protected PlatformSqlGen platformSqlGen;


    public String getRowGetSql(NsiQuery query, BoolExp filter) {
        return getRowGetSql(query, false, filter);
    }

    public String getRowGetSql(NsiQuery query, boolean lock, BoolExp filter) {
        return getRowGetSql(query, lock, RefAttrsType.REF_OBJECT_ATTRS, filter);
    }

    public String getRowGetSql(NsiQuery query, boolean lock, RefAttrsType refAttrsType, BoolExp filter) {
        Condition filterCondition = platformSqlGen.getWhereCondition(query, filter); //todo сплюснуть

        if (lock && platformSqlGen.isLockSupported()) {
            Collection<? extends SelectField<?>> selectFields = getSelectFields(query, false);
            Table<?> fromSource = createFromSource(query, null);
            SelectJoinStep<Record> baseQuery = getQueryBuilder().select(selectFields).from(fromSource);

            if (filterCondition != null) {
                baseQuery.where(filterCondition);
            }
            return baseQuery.forUpdate().getSQL();
        } else {
            SelectJoinStep<?> baseQuery = createBaseQuery(query, true, null, refAttrsType);

            if (filterCondition != null) {
                baseQuery.where(filterCondition);
            }
            return baseQuery.getSQL();
        }
    }

    protected DSLContext getQueryBuilder() {
        return DSL.using(SQLDialect.DEFAULT, platformSqlGen.getJooqSettings());
    }

    protected SelectJoinStep<?> createBaseQuery(NsiQuery query, boolean includeRefFields, String sourceQueryName, RefAttrsType refAttrsType) {
        Collection<? extends SelectField<?>> selectFields = getSelectFields(query, true, refAttrsType);
        Table<?> fromSource = createFromSource(query, sourceQueryName);
        SelectJoinStep<Record> selectJoinStep = getQueryBuilder().select(selectFields).from(fromSource);

        return addRefAttrJoins(query, selectJoinStep);
    }

    private Table<?> createFromSource(NsiQuery query, String sourceQueryName) {
        // если запрос не задан и нет таблицы, используем запрос по умолчанию
        if (query.getDict().getTable() == null && sourceQueryName == null) {
            sourceQueryName = NsiQuery.MAIN_QUERY;
        }

        Table<?> fromSource;
        NsiConfigDict dict = query.getDict();
        if (sourceQueryName == null) {
            fromSource = table(dict.getTable());
        } else {
            fromSource = table("( " + dict.getSourceQuery(sourceQueryName).getSql() + " ) ");
        }
        return fromSource.as(NsiQuery.MAIN_ALIAS);
    }

    protected SelectJoinStep<?> addRefAttrJoins(NsiQuery query, SelectJoinStep<?> selectJoinStep) {
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            // включаем поля RefObject атрибутов
            if (attr.getType() == MetaAttrType.REF) {
                SelectOnStep<?> beforeOn;
                NsiConfigDict refDict = attr.getRefDict();
                if (refDict.getTable() != null) {
                    beforeOn = selectJoinStep.leftOuterJoin(table(refDict.getTable()).as(queryAttr.getRefAlias()));
                } else {
                    NsiConfigSourceQuery defaultQuery = refDict.getSourceQuery(NsiQuery.MAIN_QUERY);
                    Preconditions.checkNotNull(defaultQuery);
                    beforeOn = selectJoinStep.leftOuterJoin(table("( " + defaultQuery.getSql() + " ) ").as(queryAttr.getRefAlias()));
                }
                NsiConfigAttr refIdAttr = refDict.getIdAttr();

                Condition cond = createJoinFieldCondition(queryAttr, attr.getFields().get(0), refIdAttr.getFields().get(0));

                for (int i = 1; i < attr.getFields().size(); i++) {
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
            if (includeRefFields && dict.isAttrHasRefAttrs(attr)) {
                List<NsiConfigAttr> refAttrs = attr.getRefDict().getRefAttrs(refAttrsType);
                if (refAttrs != null) {
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

    @Override
    public String getRowInsertSql(NsiQuery query, boolean useSeq) {
        return platformSqlGen.getRowInsertSql(query, useSeq);
    }

    @Override
    public String getRowDeleteSql(NsiQuery query, BoolExp filter) {
        return platformSqlGen.getRowDeleteSql(query, filter);
    }

    public String getRowUpdateSql(NsiQuery query, BoolExp filter) {
        return platformSqlGen.getRowUpdateSql(query, filter);
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
        Condition filterCondition = platformSqlGen.getWhereCondition(query, filter);
        if (filterCondition != null) {
            baseQuery.where(filterCondition);
        }

        Collection<? extends SortField<?>> sortFields = getSortFields(query, sortList);
        if (sortFields != null) {
            baseQuery.orderBy(sortFields);
        }

        if (size != -1) {
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


    protected Collection<? extends SortField<?>> getSortFields(NsiQuery query, List<SortExp> sortList) {
        if (sortList == null) {
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

        Condition filterCondition = platformSqlGen.getWhereCondition(query, filter);

        if (filterCondition != null) {
            baseQueryPart.where(filterCondition);
        }

        return baseQueryPart.getSQL();
    }

    public void setPlatformSqlGen(PlatformSqlGen platformSqlGen) {
        this.platformSqlGen = platformSqlGen;
    }


}
