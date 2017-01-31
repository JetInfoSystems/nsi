package jet.nsi.migrator.hibernate;

import com.beust.jcommander.Strings;
import jet.nsi.migrator.platform.PlatformMigrator;
import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Exportable;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.UniqueConstraintSchemaUpdateStrategy;
import org.hibernate.tool.schema.extract.spi.ColumnInformation;
import org.hibernate.tool.schema.extract.spi.DatabaseInformation;
import org.hibernate.tool.schema.extract.spi.ForeignKeyInformation;
import org.hibernate.tool.schema.extract.spi.IndexInformation;
import org.hibernate.tool.schema.extract.spi.SequenceInformation;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.hibernate.tool.schema.internal.exec.GenerationTarget;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.Exporter;
import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Класс реализует логику актуализации структуры СУБД Стандартный класс не
 * подошел, потому что его код содержал ошибку
 */
public class NsiSchemaMigratorImpl implements SchemaMigrator {
    
    private static final String MODIFY_OPERATION = "alter column";

    @Override
    public void doMigration(Metadata metadata, ExecutionOptions options, TargetDescriptor targetDescriptor) {
        // TODO Auto-generated method stub
        
    }
    
    
    
    public void doMigration(
            Metadata metadata,
            DatabaseInformation existingDatabase,
            boolean createNamespaces,
            List<GenerationTarget> targets,
            PlatformMigrator platformMigrator) throws SchemaManagementException {
        
        for (GenerationTarget target : targets) {
            target.prepare();
        }
        
        doMigrationToTargets(metadata, existingDatabase, createNamespaces, targets, platformMigrator);
        
        for (GenerationTarget target : targets) {
            target.release();
        }
        System.out.println("i finished migration");
    }
    
    protected void doMigrationToTargets(
            Metadata metadata,
            DatabaseInformation existingDatabase,
            boolean createNamespaces,
            List<GenerationTarget> targets, PlatformMigrator platformMigrator) {
        final Set<String> exportIdentifiers = new HashSet<String>(50);
        
        final Database database = metadata.getDatabase();
        boolean tryToCreateCatalogs = false;
        boolean tryToCreateSchemas = false;
        
        if (createNamespaces) {
            if (database.getJdbcEnvironment().getDialect().canCreateSchema()) {
                tryToCreateSchemas = true;
            }
            if (database.getJdbcEnvironment().getDialect().canCreateCatalog()) {
                tryToCreateCatalogs = true;
            }
        }
        
        Set<Identifier> exportedCatalogs = new HashSet<Identifier>();
        for (Namespace namespace : database.getNamespaces()) {
            if (tryToCreateCatalogs || tryToCreateSchemas) {
                if (tryToCreateCatalogs) {
                    final Identifier catalogLogicalName = namespace.getName().getCatalog();
                    final Identifier catalogPhysicalName = namespace.getPhysicalName().getCatalog();
                    
                    if (catalogPhysicalName != null && !exportedCatalogs.contains(catalogLogicalName) && !existingDatabase
                                .catalogExists(catalogLogicalName)) {
                        applySqlStrings(
                                database.getJdbcEnvironment().getDialect().getCreateCatalogCommand(
                                        catalogPhysicalName.render(
                                                        database.getJdbcEnvironment().getDialect()
                                        )
                                ),
                                targets,
                                false
                        );
                        exportedCatalogs.add(catalogLogicalName);
                    }
                }
                
                if (tryToCreateSchemas 
                                && namespace.getPhysicalName().getSchema() != null
                                && !existingDatabase.schemaExists(namespace.getName())) {
                    applySqlStrings(
                            database.getJdbcEnvironment().getDialect().getCreateSchemaCommand(
                                            namespace.getPhysicalName()
                                                            .getSchema()
                                                            .render(database.getJdbcEnvironment().getDialect())
                            ),
                            targets,
                            false
                    );
                }
            }

            for (Sequence sequence : namespace.getSequences()) {
                System.out.println("wow! sequence!"+sequence.getName());
                checkExportIdentifier(sequence, exportIdentifiers);
                final SequenceInformation sequenceInformation = existingDatabase.getSequenceInformation(sequence.getName());
                if (sequenceInformation != null) {
                    // nothing we really can do...
                    continue;
                }
                
                applySqlStrings(
                                database.getJdbcEnvironment().getDialect().getSequenceExporter().getSqlCreateStrings(
                                                sequence,
                                                metadata
                                ), 
                                targets,
                                false
                );
            }

            // first pass
            for (Table table : namespace.getTables()) {
                if(table.getName().contains("seq")){
                    System.out.println("continue:"+table.getName());
                    continue;
                }
                platformMigrator.setPrimaryKey(table);

                if (!table.isPhysicalTable()) {
                    continue;
                }

                checkExportIdentifier(table, exportIdentifiers);
                final TableInformation tableInformation = existingDatabase.getTableInformation(table.getQualifiedTableName());
                if (tableInformation != null && !tableInformation.isPhysicalTable()) {
                    continue;
                }
                if (tableInformation == null) {
                    createTable(table, metadata, targets);
                } else {
                    migrateTable(table, tableInformation, targets, metadata, platformMigrator);
                }
            }
            
            // second pass
            for (Table table : namespace.getTables()) {
                if (!table.isPhysicalTable()) {
                    continue;
                }
                final TableInformation tableInformation = existingDatabase.getTableInformation(table.getQualifiedTableName());
                
                //CHANGED: removed, looks like smels
//                if ( tableInformation == null ) {
//                    // big problem...
//                    throw new SchemaManagementException( "BIG PROBLEM" );
//                }
//                if ( !tableInformation.isPhysicalTable() ) {
//                    continue;
//                }
            
                applyIndexes(table, tableInformation, metadata, targets);
                applyUniqueKeys(table, tableInformation, metadata, targets);

                if (platformMigrator.isSupportForeignKey()) {
                    applyForeignKeys(table, tableInformation, metadata, targets);
                }
            }
        }
    }
    
    private void createTable(Table table, Metadata metadata, List<GenerationTarget> targets) {
        applySqlStrings(
                        metadata.getDatabase().getDialect().getTableExporter().getSqlCreateStrings(table, metadata),
                        targets,
                        false
        );
    }
    
    private void migrateTable(
            Table table,
            TableInformation tableInformation,
            List<GenerationTarget> targets,
            Metadata metadata, PlatformMigrator platformMigrator) {
        final Database database = metadata.getDatabase();
        final JdbcEnvironment jdbcEnvironment = database.getJdbcEnvironment();
        final Dialect dialect = jdbcEnvironment.getDialect();
        
        // noinspection unchecked
        //CHANGED: table.sqlAlterStrings(...) - > sqlAlterStrings(table,...) for modify operation (for  example change length of field)
        applySqlStrings(sqlAlterStrings(table, 
                                    dialect,
                                    metadata,
                                    tableInformation,
                                    getDefaultCatalogName(database),
                                    getDefaultSchemaName(database),
                                    platformMigrator
                        ), 
                        targets,
                        false
        );
    }
    
    private void applyIndexes(Table table, TableInformation tableInformation, Metadata metadata, List<GenerationTarget> targets) {
        final Exporter<Index> exporter = metadata.getDatabase().getJdbcEnvironment().getDialect().getIndexExporter();
        
        final Iterator<Index> indexItr = table.getIndexIterator();
        while (indexItr.hasNext()) {
            final Index index = indexItr.next();
            //CHANGED: using google guava instead helper
            if (Strings.isStringEmpty(index.getName())) {
                continue;
            }
            
            // CHANGED: Added more checks, but while its used by second pass - table information must be not null
            if (tableInformation != null) {
                final IndexInformation existingIndex = findMatchingIndex(index, tableInformation);
                if (existingIndex != null) {
                    continue;
                }
            }
            
            applySqlStrings(
                            exporter.getSqlCreateStrings(index, metadata),
                            targets,
                            false
            );
        }
    }
    
    private IndexInformation findMatchingIndex(Index index, TableInformation tableInformation) {
        return tableInformation.getIndex(Identifier.toIdentifier(index.getName()));
    }
    
    private UniqueConstraintSchemaUpdateStrategy uniqueConstraintStrategy;
    
    private void applyUniqueKeys(Table table, TableInformation tableInfo, Metadata metadata, List<GenerationTarget> targets) {
        if (uniqueConstraintStrategy == null) {
            uniqueConstraintStrategy = determineUniqueConstraintSchemaUpdateStrategy(metadata);
        }
        
        if (uniqueConstraintStrategy == UniqueConstraintSchemaUpdateStrategy.SKIP) {
            return;
        }
        
        final Dialect dialect = metadata.getDatabase().getJdbcEnvironment().getDialect();
        final Exporter<Constraint> exporter = dialect.getUniqueKeyExporter();
        
        //CHANGED: added type of iterator element
        final Iterator<UniqueKey> ukItr = table.getUniqueKeyIterator();
        while (ukItr.hasNext()) {
            final UniqueKey uniqueKey = (UniqueKey) ukItr.next();
            // Skip if index already exists. Most of the time, this
            // won't work since most Dialects use Constraints. However,
            // keep it for the few that do use Indexes.
            //CHANGED: use google guava instead of helper
            if (tableInfo != null && !Strings.isStringEmpty(uniqueKey.getName())) {
                final IndexInformation indexInfo = tableInfo.getIndex(Identifier.toIdentifier(uniqueKey.getName()));
                if (indexInfo != null) {
                    continue;
                }
            }
            
            if (uniqueConstraintStrategy == UniqueConstraintSchemaUpdateStrategy.DROP_RECREATE_QUIETLY) {
                applySqlStrings(
                                exporter.getSqlDropStrings(uniqueKey, metadata),
                                targets,
                                true
                );
            }
            
            applySqlStrings(
                            exporter.getSqlCreateStrings(uniqueKey, metadata),
                            targets,
                            true
            );
        }
    }
    
    private UniqueConstraintSchemaUpdateStrategy determineUniqueConstraintSchemaUpdateStrategy(Metadata metadata) {
        final ConfigurationService cfgService = ((MetadataImplementor) metadata).getMetadataBuildingOptions()
                .getServiceRegistry()
                .getService(ConfigurationService.class);
        
        return UniqueConstraintSchemaUpdateStrategy.interpret(
                cfgService.getSetting(
                      AvailableSettings.UNIQUE_CONSTRAINT_SCHEMA_UPDATE_STRATEGY,
                      StandardConverters.STRING
                )
        );
    }
    
    private void applyForeignKeys(
                    Table table,
                    TableInformation tableInformation,
                    Metadata metadata,
                    List<GenerationTarget> targets) {
        final Dialect dialect = metadata.getDatabase().getJdbcEnvironment().getDialect();
        if (!dialect.hasAlterTable()) {
            return;
        }
        
        final Exporter<ForeignKey> exporter = dialect.getForeignKeyExporter();
        
        @SuppressWarnings("unchecked")
        final Iterator<ForeignKey> fkItr = table.getForeignKeyIterator();
        while (fkItr.hasNext()) {
            final ForeignKey foreignKey = fkItr.next();
            if (!foreignKey.isPhysicalConstraint()) {
                continue;
            }
            
            if (!foreignKey.isCreationEnabled()) {
                continue;
            }
            
            // CHANGED: Added more checks, but while its used by second pass - table information must be not null
            if (tableInformation != null) {
                final ForeignKeyInformation existingForeignKey = findMatchingForeignKey(foreignKey, tableInformation);
                // TODO: необходимо анализировать структуру вторичного ключа и
                // если она изменилась то пересоздавать
                //CHANGED: проверяем на не null и соблюдаем стиль с continue
                if (existingForeignKey != null) {
                    continue;
                }
            }
            
            applySqlStrings(exporter.getSqlCreateStrings(foreignKey, metadata), targets, false);
        }
    }
    
    private ForeignKeyInformation findMatchingForeignKey(ForeignKey foreignKey, TableInformation tableInformation) {
        if (foreignKey.getName() == null) {
            return null;
        }
        return tableInformation.getForeignKey(Identifier.toIdentifier(foreignKey.getName()));
    }
    
    private void checkExportIdentifier(Exportable exportable, Set<String> exportIdentifiers) {
        final String exportIdentifier = exportable.getExportIdentifier();
        if (exportIdentifiers.contains(exportIdentifier)) {
            throw new SchemaManagementException(
                    String.format(
                                    "Export identifier [%s] encountered more than once",
                                    exportIdentifier
                    )
            );
        }
        exportIdentifiers.add(exportIdentifier);
    }
    
    private static void applySqlStrings(String[] sqlStrings, List<GenerationTarget> targets, boolean quiet) {
        if (sqlStrings == null) {
            return;
        }
        
        for (String sqlString : sqlStrings) {
            applySqlString(sqlString, targets, quiet);
        }
    }
    
    private static void applySqlString(String sqlString, List<GenerationTarget> targets, boolean quiet) {
        if (sqlString == null) {
            return;
        }
        
        for (GenerationTarget target : targets) {
            try {
                System.out.println(sqlString);
                target.accept(sqlString);
            }
            catch (SchemaManagementException e) {
                if (!quiet) {
                    throw e;
                }
                // otherwise ignore the exception
            }
        }
    }
    
    private static void applySqlStrings(Iterator<String> sqlStrings, List<GenerationTarget> targets, boolean quiet) {
        if (sqlStrings == null) {
            return;
        }
        
        while (sqlStrings.hasNext()) {
            final String sqlString = sqlStrings.next();
            applySqlString(sqlString, targets, quiet);
        }
    }
    
    private String getDefaultCatalogName(Database database) {
        final Identifier identifier = database.getDefaultNamespace().getPhysicalName().getCatalog();
        return identifier == null ? null : identifier.render(database.getJdbcEnvironment().getDialect());
    }
    
    private String getDefaultSchemaName(Database database) {
        final Identifier identifier = database.getDefaultNamespace().getPhysicalName().getSchema();
        return identifier == null ? null : identifier.render(database.getJdbcEnvironment().getDialect());
    }
    
    

    
    public String getColumnOperationString(ColumnInformation columnInformation, Dialect dialect) {
        return (columnInformation == null) ? dialect.getAddColumnString() : MODIFY_OPERATION;
    }
    public String getColumnOperationTypeString(ColumnInformation columnInformation, Dialect dialect) {
        return (columnInformation == null) ? "" : "type ";
    }
    
    public Iterator<String> sqlAlterStrings(Table table, Dialect dialect, Mapping p, TableInformation tableInfo,
            String defaultCatalog, String defaultSchema, PlatformMigrator platformMigrator) throws HibernateException {
        /*if(dialect instanceof PhoenixDialect){
            return Collections.emptyIterator();
        }*/
        @SuppressWarnings("rawtypes")
        Iterator iter = table.getColumnIterator();
        List<String> results = new ArrayList<>();
        String tableName = table.getQualifiedName(dialect, defaultCatalog, defaultSchema);
        
        while (iter.hasNext()) {
            final Column column = (Column) iter.next();
            final ColumnInformation columnInfo = tableInfo
                    .getColumn(Identifier.toIdentifier(column.getName(), column.isQuoted()));
            
            if (columnInfo == null || (column.getLength() > columnInfo.getColumnSize()&&platformMigrator.isColumnEditable())) {
                // the column doesnt exist at all.

                StringBuilder alter = new StringBuilder("alter table ").append(tableName).append(' ')
                        .append(getColumnOperationString(columnInfo, dialect)).append(' ')
                        .append(column.getQuotedName(dialect)).append(' ')
                        .append(getColumnOperationTypeString(columnInfo, dialect))
                        .append(column.getSqlType(dialect, p));
                
                String defaultValue = column.getDefaultValue();
                if (defaultValue != null) {
                    alter.append(" default ").append(defaultValue);
                }
                
                if (column.isNullable()) {
                    if (columnInfo == null || !columnInfo.getNullable().toBoolean(true)) {
                        alter.append(dialect.getNullColumnString());
                    }
                } else {
                    if (columnInfo == null || columnInfo.getNullable().toBoolean(true)) {
                        alter.append(" not null");
                    }
                }
                
                if (column.isUnique()) {
                    String keyName = Constraint.generateName("UK_", table, column);
                    UniqueKey uk = table.getOrCreateUniqueKey(keyName);
                    uk.addColumn(column);
                    alter.append(dialect.getUniqueDelegate().getColumnDefinitionUniquenessFragment(column));
                }
                
                if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
                    alter.append(" check(").append(column.getCheckConstraint()).append(")");
                }
                
                String columnComment = column.getComment();
                if (columnComment != null) {
                    alter.append(dialect.getColumnComment(columnComment));
                }
                
                alter.append(dialect.getAddColumnSuffixString());
                
                results.add(alter.toString());
            }
            
        }
        
        if (results.isEmpty()) {
            Logger.getLogger(SchemaUpdate.class).debugf("No alter strings for table : %s", table.getQuotedName());
        }
        
        return results.iterator();
    }

}
