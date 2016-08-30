package jet.nsi.migrator.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitIndexNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;
import org.hibernate.boot.model.naming.NamingHelper;


/**
 * Используется для формирования наименований для ограничений
 * Старндартный вариант не подошел потому что не включает в имя таблицу и по имени ограничения непонятно к
 * какой таблице оно относится
 */
public class NsiImplicitNamingStrategyImpl extends
        ImplicitNamingStrategyJpaCompliantImpl {

    private static final long serialVersionUID = 1L;

    @Override
    public Identifier determineForeignKeyName(
            ImplicitForeignKeyNameSource source) {
        return toIdentifier(
                compose("fk",
                        source.getTableName().render(),
                        NamingHelper.INSTANCE.generateHashedFkName("",
                                source.getTableName(),
                                source.getReferencedTableName(),
                                source.getColumnNames()),30),
                source.getBuildingContext());
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        return toIdentifier(
                compose("uk", source.getTableName().render(), NamingHelper.INSTANCE
                        .generateHashedConstraintName("",
                                source.getTableName(), source.getColumnNames()),30),
                source.getBuildingContext());
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        return toIdentifier(
                compose("idx", source.getTableName().render(), NamingHelper.INSTANCE
                        .generateHashedConstraintName("",
                                source.getTableName(), source.getColumnNames()),30),
                source.getBuildingContext());
    }

    public static String compose(String prefix, String tableName, String suffix, int maxLen) {
        String a = new StringBuilder().append(prefix).append("_").append(tableName).toString();
        String b = new StringBuilder().append("_").append(Integer.toHexString(suffix.hashCode()).toUpperCase()).toString();
        if(a.length() + b.length() > 30) {
            a = a.substring(0, a.length() + b.length() - 30);
        }
        return a + b;
    }
    
}
