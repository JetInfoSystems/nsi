package jet.isur.nsi.api.sql;

import java.sql.Connection;
import java.util.List;

import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.SortExp;

public interface SqlDao {

    public DictRow get(Connection connection, NsiQuery query, DictRowAttr id);

    public List<DictRow> list(Connection connection, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size);

    public long count(Connection connection, NsiQuery query, BoolExp filter);

    public DictRow insert(Connection connection, NsiQuery query, DictRow data);

    public DictRow update(Connection connection, NsiQuery query, DictRow data);

}
