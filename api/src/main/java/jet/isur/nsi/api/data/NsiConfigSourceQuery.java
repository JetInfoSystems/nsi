package jet.isur.nsi.api.data;

import jet.isur.nsi.api.model.MetaSourceQuery;

public class NsiConfigSourceQuery {
    private final String sql;

    public NsiConfigSourceQuery(MetaSourceQuery query) {
        this.sql = query.getSql();
    }

    public String getSql() {
        return sql;
    }
}
