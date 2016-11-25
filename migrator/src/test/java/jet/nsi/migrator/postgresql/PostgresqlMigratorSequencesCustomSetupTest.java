package jet.nsi.migrator.postgresql;

import static jet.nsi.common.migrator.config.MigratorParams.key;
import static jet.nsi.common.migrator.config.MigratorParams.BASE;
import static jet.nsi.common.migrator.config.MigratorParams.PATH;
import static jet.nsi.common.migrator.config.MigratorParams.CHANGE_LOG;
import static jet.nsi.common.migrator.config.MigratorParams.DB;
import static jet.nsi.common.migrator.config.MigratorParams.LIQUIBASE;
import static jet.nsi.common.migrator.config.MigratorParams.LOG_PREFIX;
import static jet.nsi.common.migrator.config.MigratorParams.USE_SEQUENCE_AS_DEFAULT_VALUE_FOR_ID;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.jooq.exception.DataAccessException;
import org.junit.Test;
import org.postgresql.util.PSQLException;

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

public class PostgresqlMigratorSequencesCustomSetupTest extends BaseSqlTest{

    private static final String DB_IDENT = "nsi.postgresql95";
    private static final String TEST_NSI_PREFIX = "TEST_NSI_";
    private static final String LIQUIBASE_CHANGE_LOG_BASE_PATH = "with_empty_liquibase_changelogs";
    private static final String IS_USE_SEQUENCE_AS_DEFAULT_VALUE_FOR_ID = "true";
    
    

    private PlatformMigrator platformMigrator;
    //private PostgresqlFtsModule ftsModule;

    public PostgresqlMigratorSequencesCustomSetupTest() {
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
        properties.setProperty(key(DB,dbIdent,USE_SEQUENCE_AS_DEFAULT_VALUE_FOR_ID),
                                    IS_USE_SEQUENCE_AS_DEFAULT_VALUE_FOR_ID);
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
    public void migratorTest() throws Exception {
        setupMigrator("src/test/resources/metadata/migrator");

        NsiConfigDict dict1 = config.getDict("dict1");
        NsiConfigDict dict2 = config.getDict("dict2");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict2, connection);
            platformMigrator.dropTable(dict1, connection);
            platformMigrator.dropSeq(dict2, connection);
            platformMigrator.dropSeq(dict1, connection);

            platformMigrator.dropTable("TEST_NSI_PREPARE_LOG", connection);
            platformMigrator.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }

        {
            Migrator migrator = new Migrator (config, dataSource, params, platformMigrator);
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v1");

            List<String> actions = rec.getActions();
            Assert.assertEquals(5, actions.size());
            Assert.assertEquals("create sequence seq_table1 start 1 increment 1", actions.get(0));
            Assert.assertEquals("create sequence seq_table2 start 1 increment 1", actions.get(1));
            Assert.assertEquals("create table table1 (id int8 default nextval('seq_table1') not null, f1 varchar(100), is_deleted char(1), last_change timestamp, last_user int8, VERSION int8, primary key (id))", actions.get(2));
            Assert.assertEquals("create table table2 (id int8 default nextval('seq_table2') not null, dict1_id int8, is_deleted char(1), last_change timestamp, last_user int8, name char(100), VERSION int8, primary key (id))", actions.get(3));
            Assert.assertEquals("alter table table2 add constraint fk_table2_FE52C689 foreign key (dict1_id) references table1", actions.get(4));
            
        }

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict2, connection);
            platformMigrator.dropTable(dict1, connection);
            platformMigrator.dropSeq(dict2, connection);
            platformMigrator.dropSeq(dict1, connection);

            platformMigrator.dropTable("TEST_NSI_PREPARE_LOG", connection);
            platformMigrator.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }
    }
}
