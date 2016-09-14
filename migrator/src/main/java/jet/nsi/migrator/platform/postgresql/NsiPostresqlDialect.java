package jet.nsi.migrator.platform.postgresql;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL95Dialect;

public class NsiPostresqlDialect extends PostgreSQL95Dialect {
    
    public NsiPostresqlDialect() {
        super();
        registerCharacterTypeMappings();
        registerNumericTypeMappings();
    }
    
    
    protected void registerCharacterTypeMappings() {
        registerColumnType( Types.CHAR, 4000, "char($l)" );
    }

    protected void registerNumericTypeMappings() {
        registerColumnType( Types.BOOLEAN, "char(1)" );
    }
    
}
