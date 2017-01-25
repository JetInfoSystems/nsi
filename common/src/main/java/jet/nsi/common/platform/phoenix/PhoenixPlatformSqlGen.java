package jet.nsi.common.platform.phoenix;

import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.data.NsiQueryAttr;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlGen;
import jet.nsi.common.data.NsiDataException;
import jet.nsi.common.platform.DefaultPlatformSqlGen;

import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

public class PhoenixPlatformSqlGen extends DefaultPlatformSqlGen {

    public PhoenixPlatformSqlGen(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }

    @Override
    public String getRowInsertSql(NsiQuery query, boolean useSeq) {
        /*Optional<NsiQueryAttr> optional= query.getAttrs().stream().filter(attr->attr.getAttr()==query.getDict().getIdAttr()).findFirst();
        if(!optional.isPresent()){
            query.addAttrs()
            String val = UUID.randomUUID().toString();

        }*/

        return getRowUpdateSql(query);
//        InsertSetStep<?> insertSetStep = getQueryBuilder().insertInto(table(query.getDict().getTable()));
//        InsertSetMoreStep<?> insertSetMoreStep = null;
//        NsiConfigDict dict = query.getDict();
////        List<NsiConfigField> idFields = dict.getIdAttr().getFields();
//        for (NsiQueryAttr queryAttr : query.getAttrs()) {
//            NsiConfigAttr attr = queryAttr.getAttr();
//            if (attr == query.getDict().getIdAttr() && useSeq) {
//                if (attr.getFields().size() > 1) {
//                    throw new NsiDataException("use seq possible for id attr with one field only");
//                }
//                // seq
//                insertSetMoreStep = insertSetStep.set(field(idFields.get(0).getName()), sequenceFunсtion("seq_" + dict.getTable(), PlatformSqlGen.NEXTVAL));
//            } else {
//                for (NsiConfigField field : attr.getFields()) {
//                    insertSetMoreStep = insertSetStep.set(field(field.getName(), String.class), val(""));
//                }
//            }
//        }
//        if(insertSetMoreStep != null) {
//            if(idFields.size()==1) {
//                insertSetMoreStep.returning(getReturningFields(query)).getSQL();
//            }
//            return insertSetMoreStep.getSQL();
//        } else {
//            throw new NsiDataException("no attrs found");
//        }
    }

    @Override
    public String getRowUpdateSql(NsiQuery query) {
        NsiConfigDict dict = query.getDict();
//        UpdateSetFirstStep<?> updateSetFirstStep = getQueryBuilder()
//                .update(table(dict.getTable()));
//        UpdateSetMoreStep<?> updateSetMoreStep = null;
        StringBuilder mainQuery = new StringBuilder();
        StringBuilder binds = new StringBuilder(" values (");

        mainQuery.append("UPSERT INTO ")
                .append(dict.getTable())
                .append(" (");
        boolean useSeq = true;//todo
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
/*            if (attr == query.getDict().getIdAttr() && useSeq) {
                if (attr.getFields().size() > 1) {
                    throw new NsiDataException("use seq possible for id attr with one field only");
                }
//                mainQuery.append(attr.getFields().get(0).getName()+", ");
//                binds.append("NEXT VALUE FOR seq_" + dict.getTable()+",");
                // seq
//                insertSetMoreStep = insertSetStep.set(field(idFields.get(0).getName()), sequenceFunсtion("seq_" + dict.getTable(), PlatformSqlGen.NEXTVAL));
            } else {*/
//            if(attr != query.getDict().getIdAttr()) {
                for (NsiConfigField field : attr.getFields()) {
                    mainQuery.append(field.getName()).append(", ");
                    binds.append("?,");
//                    updateSetMoreStep  = updateSetFirstStep.set(field(field.getName(),String.class),val(""));
//                }
            }
        }
        mainQuery.setLength(mainQuery.length() - 2);
        binds.setLength(binds.length() - 1);

        mainQuery.append(")");
        binds.append(")");
        mainQuery.append(binds);
//        if(updateSetMoreStep != null) {
//        mainQuery.append(" where ").append(query.getDict().getIdAttr().getName()).append("=?");
//            Condition condition = getIdCondition(query, "");
//            return updateSetMoreStep.where(condition).getSQL();
//        } else {
//            throw new NsiDataException("no attrs found");
//        }
        return mainQuery.toString();
    }

/*    @SuppressWarnings("unchecked")
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
    
    
    @Override
    public Object sequenceFunсtion(String name, String seqFunction) {
        switch (seqFunction) {
        case NEXTVAL:
            return field(NEXTVAL + "('" + sequence(name) + "')");
        case CURRVAL:
            return field(CURRVAL + "('" + sequence(name) + "')");
        default:
            throw new NsiDataException("Invalid function for using with sequence: " + seqFunction);
        }
    }*/
}
