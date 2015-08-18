package jet.isur.nsi.migrator.hibernate;

import java.sql.Types;

import org.hibernate.dialect.Oracle10gDialect;

/**
 * Задаем отображение типов java sql types на типы СУБД
 * Базовый класс неподходит, потому что CHAR всегда мапился на CHAR(1) а BOOLEAN на NUMBER(1)
 */
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
