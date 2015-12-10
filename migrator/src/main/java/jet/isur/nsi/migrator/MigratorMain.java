package jet.isur.nsi.migrator;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.common.base.Preconditions;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.generator.DBAppender;
import jet.isur.nsi.generator.Generator;
import jet.isur.nsi.generator.GeneratorParams;
import jet.isur.nsi.generator.plugin.GeneratorPlugin;
import jet.isur.nsi.migrator.args.CommonArgs;
import jet.isur.nsi.migrator.args.CreateTablespaceCmd;
import jet.isur.nsi.migrator.args.CreateUserCmd;
import jet.isur.nsi.migrator.args.CreateUserProfileCmd;
import jet.isur.nsi.migrator.args.DropTablespaceCmd;
import jet.isur.nsi.migrator.args.DropUserCmd;
import jet.isur.nsi.migrator.args.GrantUserCmd;
import jet.isur.nsi.migrator.args.RollbackCmd;
import jet.isur.nsi.migrator.args.RunGeneratorCmd;
import jet.isur.nsi.migrator.args.RunGeneratorPluginCmd;
import jet.isur.nsi.migrator.args.TagCmd;
import jet.isur.nsi.migrator.args.UpdateCmd;
import jet.isur.nsi.testkit.utils.DaoUtils;
import liquibase.precondition.Precondition;

public class MigratorMain {

    private static final String IDENT_ISUR = "isur";
    private static final String CMD_UPDATE = "update";
    private static final String CMD_ROLLBACK = "rollback";
    private static final String CMD_TAG = "tag";
    private static final String CMD_CREATE_TABLESPACE = "createTablespace";
    private static final String CMD_DROP_TABLESPACE = "dropTablespace";
    private static final String CMD_CREATE_USER = "createUser";
    private static final String CMD_GRANT_USER = "grantUser";
    private static final String CMD_DROP_USER = "dropUser";
    private static final String CMD_RUN_GENERATOR = "runGenerator";
    private static final String CMD_RUN_GENERATOR_PLUGIN = "runGeneratorPlugin";
    private static final String CMD_CREATE_USER_PROFILE = "createUserProfile";

    private static final Logger log = LoggerFactory.getLogger(MigratorMain.class);

    public static void main(String[] args) throws Exception {
        JCommander jc = new JCommander();
        CommonArgs commonArgs = new CommonArgs();
        jc.addObject(commonArgs);
        UpdateCmd updateCmd = new UpdateCmd();
        jc.addCommand(CMD_UPDATE, updateCmd);
        RollbackCmd rollbackCmd = new RollbackCmd();
        jc.addCommand(CMD_ROLLBACK, rollbackCmd);
        TagCmd tagCmd = new TagCmd();
        jc.addCommand(CMD_TAG, tagCmd);
        CreateTablespaceCmd createTablespaceCmd = new CreateTablespaceCmd();
        jc.addCommand(CMD_CREATE_TABLESPACE, createTablespaceCmd);
        DropTablespaceCmd dropTablespaceCmd = new DropTablespaceCmd();
        jc.addCommand(CMD_DROP_TABLESPACE, dropTablespaceCmd);
        CreateUserCmd createUserCmd = new CreateUserCmd();
        jc.addCommand(CMD_CREATE_USER, createUserCmd);
        GrantUserCmd grantUserCmd = new GrantUserCmd();
        jc.addCommand(CMD_GRANT_USER, grantUserCmd);
        DropUserCmd dropUserCmd = new DropUserCmd();
        jc.addCommand(CMD_DROP_USER, dropUserCmd);
        RunGeneratorCmd runGeneratorCmd = new RunGeneratorCmd();
        jc.addCommand(CMD_RUN_GENERATOR, runGeneratorCmd);
        RunGeneratorPluginCmd runGeneratorPluginCmd = new RunGeneratorPluginCmd();
        jc.addCommand(CMD_RUN_GENERATOR_PLUGIN, runGeneratorPluginCmd);
        CreateUserProfileCmd createUserProfileCmd = new CreateUserProfileCmd();
        jc.addCommand(CMD_CREATE_USER_PROFILE, createUserProfileCmd);
        jc.parse(args);

        String command = jc.getParsedCommand();
        if (command == null) {
            jc.usage();
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileReader(commonArgs.getCfg()));

        MigratorParams params = new MigratorParams(properties);

        switch (command) {
        case CMD_UPDATE:
            doUpdateCmd(updateCmd, params, properties);
            break;
        case CMD_ROLLBACK:
            doRollbackCmd(rollbackCmd, params, properties);
            break;
        case CMD_TAG:
            doTagCmd(tagCmd, params, properties);
            break;
        case CMD_CREATE_USER_PROFILE:
            doCreateUserProfile(createUserProfileCmd, params, properties);
            break;
        case CMD_CREATE_TABLESPACE:
            doCreateTablespaceCmd(createTablespaceCmd, params, properties);
            break;
        case CMD_DROP_TABLESPACE:
            doDropTablespaceCmd(dropTablespaceCmd, params, properties);
            break;
        case CMD_CREATE_USER:
            doCreateUserCmd(createUserCmd, params, properties);
            break;
        case CMD_GRANT_USER:
            doGrantUserCmd(params, properties);
            break;
        case CMD_DROP_USER:
            doDropUserCmd(params, properties);
            break;
        case CMD_RUN_GENERATOR:
            doRunGeneratorCmd(runGeneratorCmd, properties);
            break;
        case CMD_RUN_GENERATOR_PLUGIN:
            doRunGeneratorPluginCmd(runGeneratorPluginCmd, properties);
            break;
        }

        log.info("SUCCESS");
    }

    private static void doRunGeneratorCmd(RunGeneratorCmd runGeneratorCmd, Properties properties) throws Exception {
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        GeneratorParams params = new GeneratorParams(properties);
        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();

        DBAppender appender = new DBAppender(dataSource, config);

        Generator generator = new Generator(config, appender, params);

        switch (runGeneratorCmd.getCmd()) {
        case Generator.CMD_APPEND_DATA:
            generator.appendData();
            break;
        case Generator.CMD_CLEAN_DATA:
            generator.cleanData();
            break;
        }

    }

    private static void doRunGeneratorPluginCmd(RunGeneratorPluginCmd runGeneratorPluginCmd,
            Properties properties) throws Exception {
        GeneratorParams params = new GeneratorParams(properties);
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();

        Class<?> pluginClass = Thread.currentThread().getContextClassLoader().loadClass(runGeneratorPluginCmd.getPluginClass());
        GeneratorPlugin plugin = (GeneratorPlugin)pluginClass.newInstance();
        plugin.execute(config, dataSource, params);
    }

    private static void doDropUserCmd(MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        DaoUtils.dropUser(connection,
                params.getUsername(IDENT_ISUR));
    }

    private static void doCreateUserCmd(CreateUserCmd createUserCmd, MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        DaoUtils.createUser(connection,
                params.getUsername(IDENT_ISUR),
                params.getPassword(IDENT_ISUR),
                params.getTablespace(createUserCmd.getTablespace()),
                params.getTempTablespace(IDENT_ISUR));
    }
    
    private static void doGrantUserCmd(MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        DaoUtils.grantUser(connection, params.getUsername(IDENT_ISUR));
    }

    private static void doDropTablespaceCmd(DropTablespaceCmd dropTablespaceCmd, MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        String tsName =  params.getTablespace(dropTablespaceCmd.getIdent());
        Preconditions.checkNotNull(tsName, "Не задано название табличного пространства для ident=%s", dropTablespaceCmd.getIdent());
        DaoUtils.dropTablespace(connection, tsName);
    }

    private static void doCreateTablespaceCmd(CreateTablespaceCmd createTablespaceCmd, MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        String tsName =  params.getTablespace(createTablespaceCmd.getIdent());
        Preconditions.checkNotNull(tsName, "Не задано название табличного пространства для ident=%s", createTablespaceCmd.getIdent());
        DaoUtils.createTablespace(connection,
                tsName,
                params.getDataFilePath(createTablespaceCmd.getIdent()) + params.getDataFileName(createTablespaceCmd.getIdent()),
                params.getDataFileSize(createTablespaceCmd.getIdent()),
                params.getDataFileAutoSize(createTablespaceCmd.getIdent()),
                params.getDataFileMaxSize(createTablespaceCmd.getIdent()));
    }

    private static void doTagCmd(TagCmd tagCmd, MigratorParams params, Properties properties) {
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();
        Migrator migrator = new Migrator(config, dataSource, params);
        migrator.tag(tagCmd.getTag());
    }

    private static void doRollbackCmd(RollbackCmd rollbackCmd,
            MigratorParams params, Properties properties) {
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();
        Migrator migrator = new Migrator(config, dataSource, params);
        migrator.rollback(rollbackCmd.getTag());
    }

    private static void doUpdateCmd(UpdateCmd updateCmd, MigratorParams params, Properties properties) {
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();
        Migrator migrator = new Migrator(config, dataSource, params);
        migrator.update(updateCmd.getTag());
    }
    
    private static void doCreateUserProfile(CreateUserProfileCmd createUserProfileCmd, MigratorParams params, Properties properties) throws SQLException {
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        try (Connection connection = dataSource.getConnection()) {
            if(DaoUtils.createUserProfile(connection, createUserProfileCmd.getLogin()) == null) {
                System.out.println("User with dn " +createUserProfileCmd.getLogin()+ " already exists");
            }
        }
    }
    

}
