package jet.isur.nsi.migrator;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.migrator.hibernate.LogActionsTargetImpl;
import jet.isur.nsi.migrator.hibernate.NsiDatabaseInformationImpl;
import jet.isur.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;
import jet.isur.nsi.migrator.hibernate.NsiOracleDialect;
import jet.isur.nsi.migrator.hibernate.NsiSchemaMigratorImpl;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.tool.schema.extract.spi.DatabaseInformation;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.hibernate.tool.schema.spi.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migrator {

    private static final Logger log = LoggerFactory.getLogger(Migrator.class);

    private final NsiConfig config;
    private final MigratorParams params;
    private final DataSource dataSource;
    private final List<Target> targets = new ArrayList<>();

    public Migrator(NsiConfig config, DataSource dataSource, MigratorParams params) {
        this.config = config;
        this.dataSource = dataSource;
        this.params = params;
    }

    public void execute() {

        try {
            StandardServiceRegistry serviceRegistry = buildStandardServiceRegistry();

            try {
                MetadataImplementor metadata = buildMetadata( serviceRegistry );
                JdbcConnectionAccess jdbcConnectionAccess = serviceRegistry.getService( JdbcServices.class ).getBootstrapJdbcConnectionAccess();
                log.info("runningHbm2ddlSchemaUpdate");

                addTarget( new LogActionsTargetImpl() );

                //addTarget( new NsiTargetDatabaseImpl( jdbcConnectionAccess ) );

                SchemaMigrator schemaMigrator = new NsiSchemaMigratorImpl();

                JdbcServices jdbcServices = serviceRegistry.getService( JdbcServices.class );
                DatabaseInformation databaseInformation;
                try {
                    databaseInformation = new NsiDatabaseInformationImpl(
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
                StandardServiceRegistryBuilder.destroy( serviceRegistry );
            }
        }
        catch (Exception e) {
            throw new MigratorException("execute", e);
        }

    }

    public void addTarget(Target target) {
        targets.add(target);
    }

    private MetadataImplementor buildMetadata(
            StandardServiceRegistry serviceRegistry) {
        MetadataSources metadataSources = new MetadataSources( serviceRegistry );

        NsiDictToHbmEntitySerializer serializer = new NsiDictToHbmEntitySerializer();

        for ( NsiConfigDict dict : config.getDicts()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            serializer.marshalTo(dict, os);
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            metadataSources.addInputStream(is);
        }

        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();

        metadataBuilder.applyImplicitNamingStrategy(new NsiImplicitNamingStrategyImpl());

        return (MetadataImplementor) metadataBuilder.build();
    }

    private StandardServiceRegistry buildStandardServiceRegistry() {
        final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
        final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder( bsr );

        Properties props = new Properties();
        // TODO: используем params
        props.put(AvailableSettings.DIALECT, NsiOracleDialect.class.getName());
        props.put(AvailableSettings.DATASOURCE, dataSource);
        ssrBuilder.applySettings( props );

        return ssrBuilder.build();
    }

}
