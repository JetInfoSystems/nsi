package jet.isur.nsi.migrator.hibernate;

import java.sql.Types;

import org.hibernate.dialect.Oracle10gDialect;

public class NsiOracleDialect extends Oracle10gDialect {

    @Override
    protected void registerCharacterTypeMappings() {
        super.registerCharacterTypeMappings();
        registerColumnType( Types.CHAR, 4000, "char($l char)" );
    }

    @Override
    protected void registerNumericTypeMappings() {
        super.registerNumericTypeMappings();
        registerColumnType( Types.BOOLEAN, "char(1 char)" );
    }
}
