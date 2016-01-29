package jet.isur.nsi.api.sql;

import java.util.List;

import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.SortExp;

public interface SqlGen {

    public String getRowGetSql(NsiQuery query);
    
    public String getRowGetSql(NsiQuery query, boolean lock);
    
    public String getRowInsertSql(NsiQuery query, boolean useSeq);

    public String getRowUpdateSql(NsiQuery query);

    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size);

    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size, String sourceQueryName);

    public String getCountSql(NsiQuery query, BoolExp filter);

    public String getCountSql(NsiQuery query, BoolExp filter, String sourceQueryName);
}
