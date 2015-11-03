package jet.isur.nsi.migrator.hibernate;

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
                        source.getTableName(),
                        NamingHelper.INSTANCE.generateHashedFkName("",
                                source.getTableName(),
                                source.getReferencedTableName(),
                                source.getColumnNames())),
                source.getBuildingContext());
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        return toIdentifier(
                compose("uk", source.getTableName(), NamingHelper.INSTANCE
                        .generateHashedConstraintName("",
                                source.getTableName(), source.getColumnNames())),
                source.getBuildingContext());
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        return toIdentifier(
                compose("idx", source.getTableName(), NamingHelper.INSTANCE
                        .generateHashedConstraintName("",
                                source.getTableName(), source.getColumnNames())),
                source.getBuildingContext());
    }

    private String compose(String prefix, Identifier tableName, String suffix) {
        String a = new StringBuilder().append(prefix).append("_").append(tableName.render()).toString();
        String b = new StringBuilder().append("_").append(Integer.toHexString(suffix.hashCode()).toUpperCase()).toString();
        if(a.length() + b.length() > 30) {
            a = a.substring(0, a.length() + b.length() - 30);
        }
        return a + b;
    }
}
