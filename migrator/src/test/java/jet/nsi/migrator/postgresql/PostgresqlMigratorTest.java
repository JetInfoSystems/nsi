package jet.nsi.migrator.postgresql;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.migrator.BaseMigratorSqlTest;
import jet.nsi.migrator.Migrator;
import jet.nsi.migrator.hibernate.RecActionsTargetImpl;
import jet.nsi.migrator.platform.postgresql.PostgresqlPlatformMigrator;
import jet.nsi.testkit.utils.PostgresqlPlatformDaoUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static jet.nsi.common.migrator.config.MigratorParams.DB;
import static jet.nsi.common.migrator.config.MigratorParams.LIQUIBASE;
import static jet.nsi.common.migrator.config.MigratorParams.LOG_PREFIX;
import static jet.nsi.common.migrator.config.MigratorParams.key;

public class PostgresqlMigratorTest extends BaseMigratorSqlTest {

    private static final String DB_IDENT = "postgres";//todo
    private static final String TEST_NSI_PREFIX = "TEST_NSI_";


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
        properties.setProperty(key(DB, LIQUIBASE, LOG_PREFIX), TEST_NSI_PREFIX);
    }

    @Override
    protected void initPlatformSpecific() {
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

        NsiConfigDict dict1 = config.getDict("dict11");
        NsiConfigDict dict2 = config.getDict("dict22");

        try (Connection connection = dataSource.getConnection()) {

            doOperation(platformMigrator::dropTable, dict2, connection);
            doOperation(platformMigrator::dropTable, dict1, connection);
            doOperation(platformMigrator::dropSeq, dict2, connection);
            doOperation(platformMigrator::dropSeq, dict1, connection);

            doOperation(platformMigrator::dropSeq, "SEQ_POSTPROC1", connection);
            doOperation(platformMigrator::dropTable, "TEST_NSI_PREPARE_LOG", connection);
            doOperation(platformMigrator::dropTable, "TEST_NSI_POSTPROC_LOG", connection);
        }

        {
            Migrator migrator = new Migrator(config, Collections.singletonList(platformMigrator), "POSTGRES");
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget(rec);
            migrator.update("v1");

            List<String> actions = rec.getActions();
            Assert.assertEquals(5, actions.size());
            Assert.assertEquals("create sequence seq_table11 start 1 increment 1", actions.get(0));
            Assert.assertEquals("create sequence seq_table22 start 1 increment 1", actions.get(1));
            Assert.assertEquals("create table table11 (id int8 not null, f1 varchar(100), is_deleted char(1), last_change timestamp, last_user int8, VERSION int8, primary key (id))", actions.get(2));
            Assert.assertEquals("create table table22 (id int8 not null, dict1_id int8, is_deleted char(1), last_change timestamp, last_user int8, name char(100), VERSION int8, primary key (id))", actions.get(3));
            Assert.assertEquals("alter table table22 add constraint fk_table22_7185384A foreign key (dict1_id) references table11", actions.get(4));

        }

        // check SEQ_POSTPROC1
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "select nextval('SEQ_POSTPROC1')");
        }

        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "ALTER TABLE TABLE11 DROP COLUMN F1");
        }

        {
            Migrator migrator = new Migrator(config, Collections.singletonList(platformMigrator), "POSTGRES");
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget(rec);
            migrator.update("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(1, actions.size());
            Assert.assertEquals("alter table table11 add column f1 varchar(100)", actions.get(0));
        }

        {
            Migrator migrator = new Migrator(config, Collections.singletonList(platformMigrator), "POSTGRES");
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget(rec);
            migrator.rollback("v2", platformMigrator);

            List<String> actions = rec.getActions();
            Assert.assertEquals(0, actions.size());
        }

        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict2, connection);
            platformMigrator.dropTable(dict1, connection);
            platformMigrator.dropSeq(dict2, connection);
            platformMigrator.dropSeq(dict1, connection);

            platformMigrator.dropSeq("SEQ_POSTPROC1", connection);

            platformMigrator.dropTable("TEST_NSI_PREPARE_LOG", connection);
            platformMigrator.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }
    }


}
