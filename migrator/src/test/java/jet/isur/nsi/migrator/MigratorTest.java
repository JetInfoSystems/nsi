package jet.isur.nsi.migrator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.migrator.hibernate.RecActionsTargetImpl;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import jet.isur.nsi.testkit.utils.DaoUtils;
import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

public class MigratorTest extends BaseSqlTest{

    private static final String IDENT_ISUR = "isur";
    private NsiConfig config;
    private String metadataPath;
    private MigratorParams params;

    @Override
    public void setup() throws Exception {
        super.setup();
        getConfiguration();

        params = new MigratorParams(properties);
    }

    public void setupMigrator(String metadataPath) throws Exception {
        this.metadataPath = metadataPath;

        File configPath = new File(metadataPath);
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
    }


    private void getConfiguration() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("project.properties");
        Properties props = new Properties();
        props.load(in);
    }


    @Test
    public void migratorTest() throws Exception {
        setupMigrator("src/test/resources/metadata/migrator");

        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.dropTable("table2", connection);
            DaoUtils.dropTable("table1", connection);
            DaoUtils.dropSeq("seq_table2", connection);
            DaoUtils.dropSeq("SEQ_TABLE1", connection);

            DaoUtils.dropSeq("SEQ_POSTPROC1", connection);

            DaoUtils.dropTable("TEST_NSI_PREPARE_LOG", connection);
            DaoUtils.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, "TEST_NSI_" );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v1");

            List<String> actions = rec.getActions();
            Assert.assertEquals(4, actions.size());
            Assert.assertEquals("create table table1 (id number(19,0) not null, f1 varchar2(100 char), "
                    + "is_deleted char(1 char), last_change date, last_user number(19,0), "
                    + "primary key (id))", actions.get(0));
            Assert.assertEquals("create table table2 (id number(19,0) not null, dict1_id number(19,0), "
                    + "is_deleted char(1 char), last_change date, last_user number(19,0), "
                    + "name char(100 char), primary key (id))", actions.get(1));
            Assert.assertEquals("alter table table2 add constraint fk_table2_FE52C689 foreign key (dict1_id) references table1", actions.get(2));
            Assert.assertEquals("create sequence seq_table2 start with 1 increment by 1", actions.get(3));
        }

        // check SEQ_POSTPROC1
        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.executeSql(connection, "select SEQ_POSTPROC1.nextval from dual");
        }

        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.executeSql(connection, "ALTER TABLE TABLE1 DROP COLUMN F1");
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, "TEST_NSI_" );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(1, actions.size());
            Assert.assertEquals("alter table table1 add f1 varchar2(100 char)",actions.get(0));
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, "TEST_NSI_" );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.rollback("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(0, actions.size());
        }

        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.dropTable("table2", connection);
            DaoUtils.dropTable("table1", connection);
            DaoUtils.dropSeq("seq_table2", connection);
            DaoUtils.dropSeq("SEQ_TABLE1", connection);

            DaoUtils.dropSeq("SEQ_POSTPROC1", connection);

            DaoUtils.dropTable("TEST_NSI_PREPARE_LOG", connection);
            DaoUtils.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }

    }

    @Test
    public void tablespaceTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put("db.isur.tablespace.name", tempName);
        try(Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties)) {
            DaoUtils.createTablespace(connection,
                    params.getTablespace(IDENT_ISUR),
                    params.getDataFileName(IDENT_ISUR), "1M", "1M", "10M");
            DaoUtils.dropTablespace(connection, params.getTablespace(IDENT_ISUR));
        }
    }

    @Test
    public void userTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put("db.isur.tablespace.name", tempName);
        properties.put("db.isur.username", tempName);
        try(Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties)) {
            DaoUtils.createTablespace(connection,
                    params.getTablespace(IDENT_ISUR),
                    params.getDataFileName(IDENT_ISUR), "1M", "1M", "10M");
            try {
                DaoUtils.createUser(connection,
                        params.getUsername(IDENT_ISUR),
                        params.getPassword(IDENT_ISUR),
                        params.getTablespace(IDENT_ISUR),
                        params.getTempTablespace(IDENT_ISUR));
            } finally {
                DaoUtils.dropTablespace(connection, params.getTablespace(IDENT_ISUR));
            }
        }
    }

    @Test
    public void createUserProfileTest() throws SQLException {
        String login = String.valueOf(System.nanoTime());
        try (Connection con = dataSource.getConnection()) {
            Long id = null;
            try {
                id = DaoUtils.createUserProfile(con, login);
                Assert.assertNotNull(id);
                Assert.assertNull(DaoUtils.createUserProfile(con, login));
            } finally {
                DaoUtils.removeUserProfile(con, id);
            }
        }
    }

    @Test
    public void changeColumnSizeTest() throws Exception {

        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.dropTable("test_size", connection);
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        setupMigrator("src/test/resources/metadata/changeColumnSize/create");
        Migrator migrator = new Migrator(config, dataSource, params, "TEST_NSI_" );
        migrator.addTarget( rec );
        migrator.update("v1");

        setupMigrator("src/test/resources/metadata/changeColumnSize/alter");
        migrator = new Migrator(config, dataSource, params, "TEST_NSI_" );
        migrator.addTarget( rec );
        migrator.update("v1");

        List<String> actions = rec.getActions();
        System.out.println(actions);
        Assert.assertEquals(2, actions.size());
        Assert.assertEquals("alter table test_size modify test char(4 char)", actions.get(1));

        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.dropTable("test_size", connection);
        }
    }


    @Test
    public void checkTypesTest() throws Exception {

        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.dropTable("dict1", connection);
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        setupMigrator("src/test/resources/metadata/check_types");
        Migrator migrator = new Migrator(config, dataSource, params, "TEST_NSI_" );
        migrator.addTarget( rec );
        migrator.update("v1");

        List<String> actions = rec.getActions();
        System.out.println(actions);
        Assert.assertEquals(1, actions.size());
        Assert.assertEquals("create table dict1 (id number(19,0) not null, f1 number(20,8), primary key (id))", actions.get(0));

        try(Connection connection = dataSource.getConnection()) {
            DaoUtils.dropTable("dict1", connection);
        }
    }

}
