package jet.isur.nsi.migrator;

import java.io.FileReader;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.migrator.args.RollbackCmd;
import jet.isur.nsi.migrator.args.TagCmd;
import jet.isur.nsi.migrator.args.UpdateCmd;
import jet.isur.nsi.migrator.args.CommonArgs;
import jet.isur.nsi.testkit.utils.DaoUtils;

import com.beust.jcommander.JCommander;

public class MigratorMain {

    private static final String CMD_UPDATE = "update";
    private static final String CMD_ROLLBACK = "rollback";
    private static final String CMD_TAG = "tag";

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

        jc.parse(args);

        String command = jc.getParsedCommand();
        if (command == null) {
            jc.usage();
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileReader(commonArgs.getCfg()));

        MigratorParams params = new MigratorParams(properties);

        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();

        DataSource dataSource = DaoUtils.createDataSource("isur", properties);

        Migrator generator = new Migrator(config, dataSource, params);

        switch (command) {
        case CMD_UPDATE:
            generator.update(updateCmd.getTag());
            break;
        default:
            break;
        }
    }

}
