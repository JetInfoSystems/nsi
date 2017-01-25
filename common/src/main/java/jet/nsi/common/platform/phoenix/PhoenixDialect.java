package jet.nsi.common.platform.phoenix;


import org.hibernate.dialect.Dialect;
import org.hibernate.tool.schema.internal.StandardTableExporter;

import java.sql.Types;

public class PhoenixDialect extends Dialect{
//    private StandardTableExporter tableExporter = new StandardTableExporter( this );

    public PhoenixDialect() {
        super();
        registerColumnType( Types.CLOB, "varchar");
        registerColumnType( Types.NUMERIC, "INTEGER");
    }
}
