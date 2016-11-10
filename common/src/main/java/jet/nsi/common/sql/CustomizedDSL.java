package jet.nsi.common.sql;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;

public class CustomizedDSL extends DSL {
    public static DSLContext using(SQLDialect dialect) {
        return new CustomizedDefaultDSLContext(dialect, null);
    }
    
    public static DSLContext using(SQLDialect dialect, Settings settings) {
        return new CustomizedDefaultDSLContext(dialect, settings);
    }
}
