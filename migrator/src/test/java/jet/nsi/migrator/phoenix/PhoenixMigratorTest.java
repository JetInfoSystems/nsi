package jet.nsi.migrator.phoenix;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.migrator.BaseMigratorSqlTest;
import jet.nsi.migrator.Migrator;
import jet.nsi.migrator.hibernate.RecActionsTargetImpl;
import jet.nsi.migrator.platform.phoenix.PhoenixPlatformMigrator;
import jet.nsi.testkit.utils.PhoenixPlatformDaoUtils;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static jet.nsi.common.migrator.config.MigratorParams.DB;
import static jet.nsi.common.migrator.config.MigratorParams.LIQUIBASE;
import static jet.nsi.common.migrator.config.MigratorParams.LOG_PREFIX;
import static jet.nsi.common.migrator.config.MigratorParams.key;

public class PhoenixMigratorTest extends BaseMigratorSqlTest {

    private static final String DB_IDENT = "phoenix";
    private static final String TEST_NSI_PREFIX = "TEST_NSI_";

    public PhoenixMigratorTest() {
        super(DB_IDENT, new PhoenixPlatformDaoUtils());
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
        platformMigrator = new PhoenixPlatformMigrator(params);
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

        {
            Migrator migrator = new Migrator(config, Collections.singletonList(platformMigrator), "PHOENIX");
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget(rec);
            migrator.update("v1");

            List<String> actions = rec.getActions();
            Assert.assertEquals(2, actions.size());
            Assert.assertEquals("create table table11 (id bigint not null, f1 varchar(100), is_deleted boolean, last_change date, last_user bigint, VERSION bigint, constraint pk primary key (id))", actions.get(0));
            Assert.assertEquals("create table table22 (id bigint not null, dict1_id bigint, is_deleted boolean, last_change date, last_user bigint, name char(100), VERSION bigint, constraint pk primary key (id))", actions.get(1));
        }


        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "ALTER TABLE TABLE11 DROP COLUMN f1");
        }

        {
            Migrator migrator = new Migrator(config, Collections.singletonList(platformMigrator), "PHOENIX");
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget(rec);
            migrator.update("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(1, actions.size());
            Assert.assertEquals("alter table table11 add  f1 varchar(100)", actions.get(0));
        }
    }

    @After
    public void clearMigration() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            NsiConfigDict dict1 = config.getDict("dict11");
            NsiConfigDict dict2 = config.getDict("dict22");
            doOperation(platformMigrator::dropTable, dict1, connection);
            doOperation(platformMigrator::dropTable, dict2, connection);
        }
    }
}
