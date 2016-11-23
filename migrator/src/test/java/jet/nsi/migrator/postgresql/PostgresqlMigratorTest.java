package jet.nsi.migrator.postgresql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

import java.util.Properties;

import org.jooq.exception.DataAccessException;
import org.junit.Ignore;
import org.junit.Test;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.common.config.MigratorParams;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.migrator.Migrator;
import jet.nsi.migrator.hibernate.RecActionsTargetImpl;
import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.migrator.platform.postgresql.PostgresqlPlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.PostgresqlPlatformDaoUtils;
import junit.framework.Assert;

public class PostgresqlMigratorTest extends BaseSqlTest{

    private static final String DB_IDENT = "nsi.postgresql95";
    private static final String TEST_NSI_PREFIX = "TEST_NSI_";

    private PlatformMigrator platformMigrator;
    //private PostgresqlFtsModule ftsModule;

    public PostgresqlMigratorTest() {
        super(DB_IDENT, new PostgresqlPlatformDaoUtils());
    }
    
    @Override
    public void setup() throws Exception {
        super.setup();
        
        Assert.assertEquals(TEST_NSI_PREFIX, params.getLogPrefix());
    }
    
    @Override
    protected void initTestCustomProperties() {
        properties.setProperty("db.liqubase.logPrefix", TEST_NSI_PREFIX);
    }

    @Override
    protected void initPlatformSpecific() {
        //ftsModule = new PostgresqlFtsModule(platformSqlDao);
        platformMigrator = new PostgresqlPlatformMigrator(params);
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

            platformMigrator.dropSeq("SEQ_POSTPROC1", connection);

            platformMigrator.dropTable("TEST_NSI_PREPARE_LOG", connection);
            platformMigrator.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        } 
        // FIXME : 
        catch (DataAccessException e) {
            String message = e.getMessage();
            if (message.startsWith("SQL [drop table ") && message.endsWith("does not exist")) {
                //do nothing
            } else {
                throw e;
            }
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v1");

            List<String> actions = rec.getActions();
            Assert.assertEquals(4, actions.size());
            Assert.assertEquals("create sequence seq_table2 start 1 increment 1", actions.get(0));
            Assert.assertEquals("create table table1 (id int8 not null, f1 varchar(100), is_deleted char(1), last_change timestamp, last_user int8, VERSION int8, primary key (id))", actions.get(1));
            Assert.assertEquals("create table table2 (id int8 not null, dict1_id int8, is_deleted char(1), last_change timestamp, last_user int8, name char(100), VERSION int8, primary key (id))", actions.get(2));
            Assert.assertEquals("alter table table2 add constraint fk_table2_FE52C689 foreign key (dict1_id) references table1", actions.get(3));
            
        }

        // check SEQ_POSTPROC1
        try(Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "select nextval('SEQ_POSTPROC1')");
        }

        try(Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "ALTER TABLE TABLE1 DROP COLUMN F1");
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(1, actions.size());
            Assert.assertEquals("alter table table1 add column f1 varchar(100)",actions.get(0));
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.rollback("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(0, actions.size());
        }

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict2, connection);
            platformMigrator.dropTable(dict1, connection);
            platformMigrator.dropSeq(dict2, connection);
            platformMigrator.dropSeq(dict1, connection);

            platformMigrator.dropSeq("SEQ_POSTPROC1", connection);

            platformMigrator.dropTable("TEST_NSI_PREPARE_LOG", connection);
            platformMigrator.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }
    }
    
    @Test
    public void changeColumnSizeTest() throws Exception {
        setupMigrator("src/test/resources/metadata/changeColumnSize/create");
        NsiConfigDict testSize = config.getDict("test_size");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(testSize, connection);
            platformMigrator.dropSeq(testSize, connection);
        } // FIXME : 
        catch (DataAccessException e) {
            String message = e.getMessage();
            if (message.startsWith("SQL [drop table ") && message.endsWith("does not exist")) {
                //do nothing
            } else {
                throw e;
            }
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

        setupMigrator("src/test/resources/metadata/changeColumnSize/alter");
        migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

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
