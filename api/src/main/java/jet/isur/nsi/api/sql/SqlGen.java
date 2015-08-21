package jet.isur.nsi.api.sql;

import java.util.List;

import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.SortExp;

public interface SqlGen {

    public String getRowGetSql(NsiQuery query);

    public String getRowInsertSql(NsiQuery query, boolean useSeq);

    public String getRowUpdateSql(NsiQuery query, DictRow data);

    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size);

    public String getCountSql(NsiQuery query, BoolExp filter);

}
