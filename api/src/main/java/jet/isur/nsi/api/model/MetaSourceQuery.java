package jet.isur.nsi.api.model;

import java.io.Serializable;

public class MetaSourceQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sql;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }


}
