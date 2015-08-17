package jet.isur.nsi.migrator;

import java.io.FileReader;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.migrator.args.ExecuteCmd;
import jet.isur.nsi.migrator.args.CommonArgs;
import jet.isur.nsi.testkit.utils.DaoUtils;

import com.beust.jcommander.JCommander;

public class MigratorMain {

    private static final String CMD_EXECUTE = "execute";

    public static void main(String[] args) throws Exception {

        JCommander jc = new JCommander();
        CommonArgs commonArgs = new CommonArgs();
        jc.addObject(commonArgs);
        ExecuteCmd executeCmd = new ExecuteCmd();
        jc.addCommand(CMD_EXECUTE, executeCmd);

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
        case CMD_EXECUTE:
            generator.execute();
            break;
        default:
            break;
        }
    }

}
