package jet.nsi.migrator.platform.postgresql;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL94Dialect;

public class NsiPostresqlDialect extends PostgreSQL94Dialect {
    
    public NsiPostresqlDialect() {
        super();
        registerCharacterTypeMappings();
        registerNumericTypeMappings();
        registerDateTimeTypeMappings();
    }
    
    
    protected void registerCharacterTypeMappings() {
        registerColumnType( Types.CHAR, 4000, "char($l)" );
    }

    protected void registerNumericTypeMappings() {
        registerColumnType( Types.BOOLEAN, "char(1)" );
    }

    protected void registerDateTimeTypeMappings() { registerColumnType( Types.DATE, "timestamp" );}
    
}
