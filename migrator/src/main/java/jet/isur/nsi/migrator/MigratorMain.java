package jet.isur.nsi.migrator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.generator.DBAppender;
import jet.isur.nsi.generator.Generator;
import jet.isur.nsi.generator.GeneratorParams;
import jet.isur.nsi.generator.dictdata.DictDataContent;
import jet.isur.nsi.generator.dictdata.DictDataFiles;
import jet.isur.nsi.generator.plugin.GeneratorPlugin;
import jet.isur.nsi.migrator.args.CreateTablespaceCmd;
import jet.isur.nsi.migrator.args.CreateUserCmd;
import jet.isur.nsi.migrator.args.DropTablespaceCmd;
import jet.isur.nsi.migrator.args.DropUserCmd;
import jet.isur.nsi.migrator.args.RollbackCmd;
import jet.isur.nsi.migrator.args.RunGeneratorCmd;
import jet.isur.nsi.migrator.args.RunGeneratorPluginCmd;
import jet.isur.nsi.migrator.args.TagCmd;
import jet.isur.nsi.migrator.args.UpdateCmd;
import jet.isur.nsi.migrator.args.CommonArgs;
import jet.isur.nsi.testkit.utils.DaoUtils;

import com.beust.jcommander.JCommander;

public class MigratorMain {

    private static final String IDENT_ISUR = "isur";
    private static final String CMD_UPDATE = "update";
    private static final String CMD_ROLLBACK = "rollback";
    private static final String CMD_TAG = "tag";
    private static final String CMD_CREATE_TABLESPACE = "createTablespace";
    private static final String CMD_DROP_TABLESPACE = "dropTablespace";
    private static final String CMD_CREATE_USER = "createUser";
    private static final String CMD_DROP_USER = "dropUser";
    private static final String CMD_RUN_GENERATOR = "runGenerator";
    private static final String CMD_RUN_GENERATOR_PLUGIN = "runGeneratorPlugin";

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
        DropUserCmd dropUserCmd = new DropUserCmd();
        jc.addCommand(CMD_DROP_USER, dropUserCmd);
        RunGeneratorCmd runGeneratorCmd = new RunGeneratorCmd();
        jc.addCommand(CMD_RUN_GENERATOR, runGeneratorCmd);
        RunGeneratorPluginCmd runGeneratorPluginCmd = new RunGeneratorPluginCmd();
        jc.addCommand(CMD_RUN_GENERATOR_PLUGIN, runGeneratorPluginCmd);

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
        case CMD_CREATE_TABLESPACE:
            doCreateTablespaceCmd(params, properties);
            break;
        case CMD_DROP_TABLESPACE:
            doDropTablespaceCmd(params, properties);
            break;
        case CMD_CREATE_USER:
            doCreateUserCmd(params, properties);
            break;
        case CMD_DROP_USER:
            doDropUserCmd(params, properties);
            break;
        case CMD_RUN_GENERATOR:
            doRunGeneratorCmd(runGeneratorCmd, properties);
            break;
        case CMD_RUN_GENERATOR_PLUGIN:
            doRunGeneratorPluginCmd(runGeneratorPluginCmd, params, properties);
            break;
        }

    }

    private static void doRunGeneratorCmd(RunGeneratorCmd runGeneratorCmd, Properties properties) throws Exception {
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        GeneratorParams params = new GeneratorParams(properties);
        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();

        DictDataFiles dictdataFiles = new DictDataFiles(params.getDictdataPath());
        DictDataContent dictdataContent = new DictDataContent();
        dictdataContent.loadDictData(dictdataFiles);

        DBAppender appender = new DBAppender(dataSource, config);

        Generator generator = new Generator(config, dictdataContent, appender, params);

        switch (runGeneratorCmd.getCmd()) {
        case Generator.CMD_APPEND_DATA:
            generator.appendData();
            break;
        case Generator.CMD_CLEAN_DATA:
            generator.cleanData();
            break;
        }

    }

    private static void doRunGeneratorPluginCmd(
            RunGeneratorPluginCmd runGeneratorPluginCmd, MigratorParams params,
            Properties properties) throws Exception {
        DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);
        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();

        Class<?> pluginClass = Thread.currentThread().getContextClassLoader().loadClass(runGeneratorPluginCmd.getPluginClass());
        GeneratorPlugin plugin = (GeneratorPlugin)pluginClass.newInstance();
        plugin.execute(config, dataSource, properties);
    }

    private static void doDropUserCmd(MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        DaoUtils.dropUser(connection,
                params.getUsername(IDENT_ISUR));
    }

    private static void doCreateUserCmd(MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        DaoUtils.createUser(connection,
                params.getUsername(IDENT_ISUR),
                params.getPassword(IDENT_ISUR),
                params.getTablespace(IDENT_ISUR),
                params.getTempTablespace(IDENT_ISUR));
    }

    private static void doDropTablespaceCmd(MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        DaoUtils.dropTablespace(connection,
                params.getTablespace(IDENT_ISUR));
    }

    private static void doCreateTablespaceCmd(MigratorParams params, Properties properties) throws SQLException {
        Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);
        DaoUtils.createTablespace(connection,
                params.getTablespace(IDENT_ISUR),
                params.getDataFilePath(IDENT_ISUR) + params.getDataFileName(IDENT_ISUR),
                params.getDataFileSize(IDENT_ISUR),
                params.getDataFileAutoSize(IDENT_ISUR),
                params.getDataFileMaxSize(IDENT_ISUR));
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

}
