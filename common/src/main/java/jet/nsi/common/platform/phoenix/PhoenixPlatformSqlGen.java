package jet.nsi.common.platform.phoenix;

import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.data.NsiQueryAttr;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.common.platform.DefaultPlatformSqlGen;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.transaction.NotSupportedException;

public class PhoenixPlatformSqlGen extends DefaultPlatformSqlGen {

    public PhoenixPlatformSqlGen(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }


    @Override
    public boolean isLockSupported() {
        return false;
    }

    @Override
    public String getRowInsertSql(NsiQuery query, boolean useSeq) {
        return getRowUpdateSql(query, null);
    }

    @Override
    public String getRowUpdateSql(NsiQuery query, BoolExp filter) {
        NsiConfigDict dict = query.getDict();

        StringBuilder mainQuery = new StringBuilder();
        StringBuilder binds = new StringBuilder(" values (");

        mainQuery.append("UPSERT INTO ")
                .append(dict.getTable())
                .append(" (");

        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();
            for (NsiConfigField field : attr.getFields()) {
                mainQuery.append(field.getName()).append(", ");
                binds.append("?,");
            }
        }

        mainQuery.setLength(mainQuery.length() - 2);
        binds.setLength(binds.length() - 1);

        mainQuery.append(")");
        binds.append(")");
        mainQuery.append(binds);

        return mainQuery.toString();
    }
}
