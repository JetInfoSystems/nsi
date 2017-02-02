package jet.nsi.migrator;

import com.google.common.base.Strings;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.common.migrator.config.MigratorParams;
import jet.nsi.migrator.hibernate.ExecuteSqlTargetImpl;
import jet.nsi.migrator.hibernate.LogActionsTargetImpl;
import jet.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;
import jet.nsi.migrator.hibernate.NsiSchemaMigratorImpl;
import jet.nsi.migrator.liquibase.LiqubaseAction;
import jet.nsi.migrator.platform.DictToHbmSerializer;
import jet.nsi.migrator.platform.PlatformMigrator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.tool.schema.extract.spi.DatabaseInformation;
import org.hibernate.tool.schema.internal.exec.GenerationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Migrator {

    private static final String LIQUIBASE_PREPARE_CHANGELOG_XML = "liquibase/prepare/changelog.xml";
    private static final String LIQUIBASE_POSTPROC_CHANGELOG_XML = "liquibase/postproc/changelog.xml";
    private static final String ACTION_ROLLBACK = "rollback";
    private static final String ACTION_UPDATE = "update";

    private static final String MIGRATIONS_PREPARE = "PREPARE";
    private static final String MIGRATIONS_POSTPROC = "POSTPROC";

    private static final Logger log = LoggerFactory.getLogger(Migrator.class);

    private final NsiConfig config;
    private final String defaultDatabase;
    private final List<PlatformMigrator> migratorList;

    private List<GenerationTarget> targets = new ArrayList<>();

    public Migrator(NsiConfig config, List<PlatformMigrator> migratorList, String defaultDatabase) {
        this.config = config;
        this.migratorList = migratorList;
        this.defaultDatabase = Strings.isNullOrEmpty(defaultDatabase) ? "postgres" : defaultDatabase;
    }

    private String composePath(String part1, String part2) {
        return Strings.isNullOrEmpty(part1) ? part2 : Paths.get(part1, part2).toString();
    }

    public void update(String tag) {
        for (PlatformMigrator platformMigrator : migratorList) {
            internalUpdate(tag, platformMigrator);
        }
    }

    private void internalUpdate(String tag, PlatformMigrator platformMigrator) {
        log.info("internalUpdate->{}", platformMigrator.getPlatform().getPlatformName());
        DataSource dataSource = platformMigrator.getDataSource();
        String liquibasePrepareChangelogFilePath = composePath(platformMigrator.getParams().getChangelogBasePath(),
                LIQUIBASE_PREPARE_CHANGELOG_XML);
        String liquibasePostprocChangelogFilePath = composePath(platformMigrator.getParams().getChangelogBasePath(),
                LIQUIBASE_POSTPROC_CHANGELOG_XML);
        try {
            String platformName = platformMigrator.getPlatform().getPlatformName();
            try (Connection connection = dataSource.getConnection()) {
                platformMigrator.onUpdateBeforePrepare(connection, config);
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateBeforePrepare(connection, model);
                }
            }
            platformMigrator.doLiquibaseUpdate(MIGRATIONS_PREPARE, liquibasePrepareChangelogFilePath, tag,
                    ACTION_UPDATE, platformMigrator.getParams().getLogPrefix(), dataSource);
            try (Connection connection = dataSource.getConnection()) {
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateAfterPrepare(connection, model);
                }
                platformMigrator.onUpdateAfterPrepare(connection, config);
            }

            StandardServiceRegistry serviceRegistry = platformMigrator.buildStandardServiceRegistry(dataSource);

            try {
                MetadataImplementor metadata = buildMetadata(serviceRegistry, platformName, platformMigrator);
                JdbcConnectionAccess jdbcConnectionAccess = serviceRegistry.getService(JdbcServices.class).getBootstrapJdbcConnectionAccess();
                log.info("runningHbm2ddlSchemaUpdate");

                addTarget(new LogActionsTargetImpl());

                addTarget(new ExecuteSqlTargetImpl(jdbcConnectionAccess));

                NsiSchemaMigratorImpl schemaMigrator = new NsiSchemaMigratorImpl();

                JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
                DatabaseInformation databaseInformation;
                try {
                    databaseInformation = new DatabaseInformationImpl(
                            serviceRegistry,
                            serviceRegistry.getService(JdbcEnvironment.class),
                            jdbcConnectionAccess,
                            metadata.getDatabase().getDefaultNamespace().getPhysicalName().getCatalog(),
                            metadata.getDatabase().getDefaultNamespace().getPhysicalName().getSchema(),
                            platformMigrator.isNeedToInitializeSequence());
                } catch (SQLException e) {
                    throw jdbcServices.getSqlExceptionHelper().convert(
                            e, "Error creating DatabaseInformation for schema migration");
                }

                schemaMigrator.doMigration(metadata, databaseInformation, true, targets, platformMigrator);

            } finally {
                cleanTargets();
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }
            try (Connection connection = dataSource.getConnection()) {
                platformMigrator.onUpdateBeforePostproc(connection, config);
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateBeforePostproc(connection, model);
                }
            }
            platformMigrator.doLiquibaseUpdate(MIGRATIONS_POSTPROC, liquibasePostprocChangelogFilePath, tag,
                    ACTION_UPDATE, platformMigrator.getParams().getLogPrefix(), dataSource);
            try (Connection connection = dataSource.getConnection()) {
                for (NsiConfigDict model : config.getDicts()) {
                    platformMigrator.onUpdateAfterPostproc(connection, model);
                }
                platformMigrator.onUpdateAfterPostproc(connection, config);
            }
            log.info("internalUpdate->ok;{}", platformMigrator.getPlatform().getPlatformName());
        } catch (Exception e) {
            throw new MigratorException(ACTION_UPDATE, e);
        }

    }

    public void rollback(String tag, PlatformMigrator platformMigrator) {
        try {
            if(!platformMigrator.isSupportRollback()){
                throw new UnsupportedOperationException(platformMigrator.getPlatform().getPlatformName()+" not supported rollback ");
            }
            String liquibasePostprocChangelogFilePath = composePath(platformMigrator.getParams().getChangelogBasePath(),
                    LIQUIBASE_POSTPROC_CHANGELOG_XML);
            doLiquibaseRollback(MIGRATIONS_POSTPROC, liquibasePostprocChangelogFilePath, tag, platformMigrator);
        } catch (Exception e) {
            throw new MigratorException(ACTION_ROLLBACK, e);
        }

    }

    public void tag(String tag, PlatformMigrator platformMigrator) {
        try {
            String liquibasePrepareChangelogFilePath = composePath(platformMigrator.getParams().getChangelogBasePath(),
                    LIQUIBASE_PREPARE_CHANGELOG_XML);
            String liquibasePostprocChangelogFilePath = composePath(platformMigrator.getParams().getChangelogBasePath(),
                    LIQUIBASE_POSTPROC_CHANGELOG_XML);
            doLiquibaseTag(MIGRATIONS_PREPARE, liquibasePrepareChangelogFilePath, tag, platformMigrator);
            doLiquibaseTag(MIGRATIONS_POSTPROC, liquibasePostprocChangelogFilePath, tag, platformMigrator);
        } catch (Exception e) {
            throw new MigratorException(ACTION_ROLLBACK, e);
        }

    }


    private void doLiquibaseRollback(String name, String file, String tag, PlatformMigrator platformMigrator) {
        LiqubaseAction la = new LiqubaseAction(composeName(platformMigrator.getParams().getLogPrefix(), name), file, platformMigrator);
        try (Connection connection = platformMigrator.getDataSource().getConnection()) {
            la.rollback(connection, tag);
        } catch (SQLException e) {
            throw new MigratorException(ACTION_ROLLBACK, e);
        }
    }

    private void doLiquibaseTag(String name, String file, String tag, PlatformMigrator platformMigrator) {
        LiqubaseAction la = new LiqubaseAction(composeName(platformMigrator.getParams().getLogPrefix(), name), file, platformMigrator);
        try (Connection connection = platformMigrator.getDataSource().getConnection()) {
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

    private MetadataImplementor buildMetadata(StandardServiceRegistry serviceRegistry, String platformName, PlatformMigrator platformMigrator) {
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        DictToHbmSerializer serializer = platformMigrator.getDictToHbmSerializer();

        for (NsiConfigDict dict : config.getDicts()) {
            // только те сущности для которых задана таблица
            if (dict.getTable() != null && (platformName.equalsIgnoreCase(dict.getDatabaseName()) || (
                    dict.getDatabaseName() == null && platformName.equalsIgnoreCase(defaultDatabase)))) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                serializer.marshalTo(dict, os);
                ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                metadataSources.addInputStream(is);
                platformMigrator.updateMetadataSources(metadataSources, dict);
                log.info("buildMetadata->add dict:{};platform:{}",dict.getName(), platformName);
            }
        }
        platformMigrator.updateMetadataSources(metadataSources, config);

        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();

        metadataBuilder.applyImplicitNamingStrategy(new NsiImplicitNamingStrategyImpl());

        return (MetadataImplementor) metadataBuilder.build();
    }

    public static PlatformMigrator createPlatformMigrator(MigratorParams params, String ident) throws Exception {
        Class<?> clasz = Thread.currentThread().getContextClassLoader().loadClass(params.getPlatformMigrator(ident));
        return (PlatformMigrator) clasz.newInstance();
    }


}
