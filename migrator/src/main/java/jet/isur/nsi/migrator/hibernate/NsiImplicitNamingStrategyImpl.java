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
        StringBuilder sb = new StringBuilder();
        return sb.append(prefix).append("_").append(tableName.render())
                .append("_").append(Integer.toString(Math.abs(suffix.hashCode())))
                .toString();
    }
}
