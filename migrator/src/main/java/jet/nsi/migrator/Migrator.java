package jet.nsi.migrator;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.tool.schema.extract.internal.DatabaseInformationImpl;
import org.hibernate.tool.schema.extract.spi.DatabaseInformation;
import org.hibernate.tool.schema.internal.exec.GenerationTarget;
//import org.hibernate.tool.schema.spi.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.migrator.hibernate.ExecuteSqlTargetImpl;
import jet.nsi.migrator.hibernate.LogActionsTargetImpl;
import jet.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;
//import jet.nsi.migrator.hibernate.ExecuteSqlTargetImpl;
//import jet.nsi.migrator.hibernate.LogActionsTargetImpl;
//import jet.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;
import jet.nsi.migrator.hibernate.NsiSchemaMigratorImpl;
import jet.nsi.migrator.liquibase.LiqubaseAction;
import jet.nsi.migrator.platform.DictToHbmSerializer;
//import jet.nsi.migrator.platform.DictToHbmSerializer;
//import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.migrator.platform.PlatformMigrator;

public class Migrator {

    private static final String LIQUIBASE_PREPARE_CHANGELOG_XML = "liquibase/prepare/changelog.xml";
    private static final String LIQUIBASE_POSTPROC_CHANGELOG_XML = "liquibase/postproc/changelog.xml";
    private static final String ACTION_ROLLBACK = "rollback";
    private static final String ACTION_UPDATE = "update";

    private static final String MIGRATIONS_PREPARE = "PREPARE";
    private static final String MIGRATIONS_POSTPROC = "POSTPROC";

    private static final Logger log = LoggerFactory.getLogger(Migrator.class);

    private final NsiConfig config;
    private final MigratorParams params;
    private final DataSource dataSource;

    private final String logPrefix;
    private final PlatformMigrator platformMigrator;
    
    private List<GenerationTarget> targets = new ArrayList<>();

    private final String liquibasePrepareChangelogFilePath;
    private final String liquibasePostprocChangelogFilePath;

    public Migrator(NsiConfig config, DataSource dataSource, MigratorParams params, PlatformMigrator platformMigrator) {
        this.config = config;
        this.dataSource = dataSource;
        this.params = params;
        this.logPrefix = params.getLogPrefix();
        this.platformMigrator = platformMigrator;
        
        if (Strings.isNullOrEmpty(params.getChangelogBasePath())) {
            this.liquibasePrepareChangelogFilePath = LIQUIBASE_PREPARE_CHANGELOG_XML;
            this.liquibasePostprocChangelogFilePath = LIQUIBASE_POSTPROC_CHANGELOG_XML;
        } else {
            this.liquibasePrepareChangelogFilePath = params.getChangelogBasePath()
                                                    + "/"
                                                    + LIQUIBASE_PREPARE_CHANGELOG_XML;
            this.liquibasePostprocChangelogFilePath = params.getChangelogBasePath()
                                                    + "/"
                                                    + LIQUIBASE_POSTPROC_CHANGELOG_XML;
        }
    }

    public void update(String tag) {

        try {
            try(Connection connection = dataSource.getConnection()) {
                platformMigrator.onUpdateBeforePrepare(connection, config);
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateBeforePrepare(connection, model);
                }
            }
            doLiquibaseUpdate(MIGRATIONS_PREPARE,liquibasePrepareChangelogFilePath, tag);
            try(Connection connection = dataSource.getConnection()) {
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateAfterPrepare(connection, model);
                }
                platformMigrator.onUpdateAfterPrepare(connection, config);
            }

            StandardServiceRegistry serviceRegistry = platformMigrator.buildStandardServiceRegistry(dataSource);

            try {
                MetadataImplementor metadata = buildMetadata( serviceRegistry );
                JdbcConnectionAccess jdbcConnectionAccess = serviceRegistry.getService( JdbcServices.class ).getBootstrapJdbcConnectionAccess();
                log.info("runningHbm2ddlSchemaUpdate");

                addTarget( new LogActionsTargetImpl() );

                addTarget( new ExecuteSqlTargetImpl( jdbcConnectionAccess ) );

                NsiSchemaMigratorImpl schemaMigrator = new NsiSchemaMigratorImpl();

                JdbcServices jdbcServices = serviceRegistry.getService( JdbcServices.class );
                DatabaseInformation databaseInformation;
                try {
                    databaseInformation = new DatabaseInformationImpl(
                            serviceRegistry,
                            serviceRegistry.getService( JdbcEnvironment.class ),
                            jdbcConnectionAccess,
                            metadata.getDatabase().getDefaultNamespace().getPhysicalName().getCatalog(),
                            metadata.getDatabase().getDefaultNamespace().getPhysicalName().getSchema()
                    );
                }
                catch (SQLException e) {
                    throw jdbcServices.getSqlExceptionHelper().convert(
                            e, "Error creating DatabaseInformation for schema migration");
                }

                schemaMigrator.doMigration( metadata, databaseInformation, true, targets );

            }
            finally {
                cleanTargets();
                StandardServiceRegistryBuilder.destroy( serviceRegistry );
            }
            try(Connection connection = dataSource.getConnection()) {
                platformMigrator.onUpdateBeforePostproc(connection, config);
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateBeforePostproc(connection, model);
                }
            }
            doLiquibaseUpdate(MIGRATIONS_POSTPROC, liquibasePostprocChangelogFilePath, tag);
            try(Connection connection = dataSource.getConnection()) {
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateAfterPostproc(connection, model);
                }
                platformMigrator.onUpdateAfterPostproc(connection, config);
            }
        }
        catch (Exception e) {
            throw new MigratorException(ACTION_UPDATE, e);
        }

    }

    public void rollback(String tag) {
        try {
            doLiquibaseRollback(MIGRATIONS_POSTPROC, liquibasePostprocChangelogFilePath, tag);
        }
        catch (Exception e) {
            throw new MigratorException(ACTION_ROLLBACK, e);
        }

    }

    public void tag(String tag) {
        try {
            doLiquibaseTag(MIGRATIONS_PREPARE, liquibasePrepareChangelogFilePath, tag);
            doLiquibaseTag(MIGRATIONS_POSTPROC, liquibasePostprocChangelogFilePath, tag);
        }
        catch (Exception e) {
            throw new MigratorException(ACTION_ROLLBACK, e);
        }

    }

    private void doLiquibaseUpdate(String name, String file, String tag) {
        LiqubaseAction la = new LiqubaseAction(composeName(logPrefix,name), file, platformMigrator);
        try(Connection connection = dataSource.getConnection()) {
            la.update(connection, tag);
        } catch (SQLException e) {
            throw new MigratorException(ACTION_UPDATE, e);
        }
    }

    private void doLiquibaseRollback(String name, String file, String tag) {
        LiqubaseAction la = new LiqubaseAction(composeName(logPrefix,name), file, platformMigrator);
        try(Connection connection = dataSource.getConnection()) {
            la.rollback(connection, tag);
        } catch (SQLException e) {
            throw new MigratorException(ACTION_ROLLBACK, e);
        }
    }

    private void doLiquibaseTag(String name, String file, String tag) {
        LiqubaseAction la = new LiqubaseAction(composeName(logPrefix,name), file, platformMigrator);
        try(Connection connection = dataSource.getConnection()) {
            la.tag(connection, tag);
        } catch (SQLException e) {
            throw new MigratorException(ACTION_ROLLBACK, e);
        }
    }

    private String composeName(String logPrefix, String name) {
        return logPrefix == null ? name : logPrefix + name;
    }

    public void addTarget(GenerationTarget target) {
        targets.add(target);
    }

    public void cleanTargets() {
        targets = new ArrayList<>();
    }

    private MetadataImplementor buildMetadata(
            StandardServiceRegistry serviceRegistry) {
        MetadataSources metadataSources = new MetadataSources( serviceRegistry );

        DictToHbmSerializer serializer = platformMigrator.getDictToHbmSerializer();

        for ( NsiConfigDict dict : config.getDicts()) {
            // только те сущности для которых задана таблица
            if(dict.getTable() != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                serializer.marshalTo(dict, os);
                ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                metadataSources.addInputStream(is);
                platformMigrator.updateMetadataSources(metadataSources, dict);
            }
        }
        platformMigrator.updateMetadataSources(metadataSources, config);

        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();

        metadataBuilder.applyImplicitNamingStrategy(new NsiImplicitNamingStrategyImpl());

        return (MetadataImplementor) metadataBuilder.build();
    }

    public static PlatformMigrator createPlatformMigrator(MigratorParams params, String ident) throws Exception {
        Class<?> clasz = Thread.currentThread().getContextClassLoader().loadClass(params.getPlatformMigrator(ident));
        return (PlatformMigrator)clasz.newInstance();
    }


}
