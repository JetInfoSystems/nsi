package jet.nsi.migrator.postgresql;

import static jet.nsi.common.migrator.config.MigratorParams.key;
import static jet.nsi.common.migrator.config.MigratorParams.BASE;
import static jet.nsi.common.migrator.config.MigratorParams.PATH;
import static jet.nsi.common.migrator.config.MigratorParams.CHANGE_LOG;
import static jet.nsi.common.migrator.config.MigratorParams.DB;
import static jet.nsi.common.migrator.config.MigratorParams.LIQUIBASE;
import static jet.nsi.common.migrator.config.MigratorParams.LOG_PREFIX;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.junit.Test;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;

import jet.nsi.migrator.Migrator;
import jet.nsi.migrator.hibernate.RecActionsTargetImpl;
import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.migrator.platform.postgresql.PostgresqlPlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.PostgresqlPlatformDaoUtils;
import junit.framework.Assert;

public class PostgresqlMigratorChangeColumnSizeTest extends BaseSqlTest{

    private static final String DB_IDENT = "nsi.postgresql95";
    private static final String TEST_NSI_PREFIX = "TEST_NSI_";
    private static final String LIQUIBASE_CHANGE_LOG_BASE_PATH = "with_empty_liquibase_changelogs";
    
    

    private PlatformMigrator platformMigrator;
    //private PostgresqlFtsModule ftsModule;

    public PostgresqlMigratorChangeColumnSizeTest() {
        super(DB_IDENT, new PostgresqlPlatformDaoUtils());
    }
    
    @Override
    public void setup() throws Exception {
        super.setup();
        
        Assert.assertEquals(TEST_NSI_PREFIX, params.getLogPrefix());
    }
    
    @Override
    protected void initTestCustomProperties() {
        properties.setProperty(key(DB,LIQUIBASE,LOG_PREFIX), TEST_NSI_PREFIX);
        properties.setProperty(key(LIQUIBASE,CHANGE_LOG,BASE,PATH), LIQUIBASE_CHANGE_LOG_BASE_PATH);
    }

    @Override
    protected void initPlatformSpecific() {
        //ftsModule = new PostgresqlFtsModule(platformSqlDao);
        platformMigrator = new PostgresqlPlatformMigrator();
        platformMigrator.setParams(params);
        platform = platformMigrator.getPlatform();
    }

    public void setupMigrator(String metadataPath) {
        File configPath = new File(metadataPath);
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
    }

    @Test
    public void changeColumnSizeTest() throws Exception {
        setupMigrator("src/test/resources/metadata/changeColumnSize/create");
        NsiConfigDict testSize = config.getDict("test_size");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(testSize, connection);
            platformMigrator.dropSeq(testSize, connection);
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

        setupMigrator("src/test/resources/metadata/changeColumnSize/alter");
        migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v2");

        List<String> actions = rec.getActions();
        log.info(actions.toString());
        Assert.assertEquals(3, actions.size());
        Assert.assertEquals("alter table test_size alter column test type char(4)", actions.get(2));

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(testSize, connection);
            platformMigrator.dropSeq(testSize, connection);
        }
    }
}
