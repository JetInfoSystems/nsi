package jet.nsi.api.sql;

import java.util.List;

import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.RefAttrsType;
import jet.nsi.api.model.SortExp;

public interface SqlGen {

    public String getRowGetSql(NsiQuery query, BoolExp filter);
    
    public String getRowGetSql(NsiQuery query, boolean lock, BoolExp filter);
    
    public String getRowGetSql(NsiQuery query, boolean lock, RefAttrsType refAttrsType, BoolExp filter);
    
    public String getRowInsertSql(NsiQuery query, boolean useSeq);

    public String getRowUpdateSql(NsiQuery query, BoolExp filter);

    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size);

    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size, String sourceQueryName);
    
    public String getListSql(NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size, String sourceQueryName, RefAttrsType refAttrsType);

    public String getCountSql(NsiQuery query, BoolExp filter);

    public String getCountSql(NsiQuery query, BoolExp filter, String sourceQueryName);
}
