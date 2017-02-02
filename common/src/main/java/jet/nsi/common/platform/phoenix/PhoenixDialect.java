package jet.nsi.common.platform.phoenix;


import org.hibernate.dialect.Dialect;
import org.hibernate.tool.schema.internal.StandardTableExporter;

import java.io.File;
import java.sql.Types;

public class PhoenixDialect extends Dialect{

    public PhoenixDialect() {
        super();
        registerColumnType( Types.CLOB, "varchar");
        registerColumnType( Types.NUMERIC, "INTEGER");
    }


    @Override
    public String getAddColumnString() {
        return "add";
    }
}
