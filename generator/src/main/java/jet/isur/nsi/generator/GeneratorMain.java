package jet.isur.nsi.generator;

import java.io.FileReader;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.generator.args.AppendDataCmd;
import jet.isur.nsi.generator.args.CleanDataCmd;
import jet.isur.nsi.generator.args.CommonArgs;
import jet.isur.nsi.generator.dictdata.DictDataContent;
import jet.isur.nsi.generator.dictdata.DictDataFiles;
import jet.isur.nsi.testkit.utils.DaoUtils;

import com.beust.jcommander.JCommander;

public class GeneratorMain {

    private static final String CMD_CLEAN_DATA = "cleanData";
    private static final String CMD_APPEND_DATA = "appendData";

    public static void main(String[] args) throws Exception {

        JCommander jc = new JCommander();
        CommonArgs commonArgs = new CommonArgs();
        jc.addObject(commonArgs);
        AppendDataCmd appendDataCmd = new AppendDataCmd();
        jc.addCommand(CMD_APPEND_DATA, appendDataCmd);
        CleanDataCmd cleanDataCmd = new CleanDataCmd();
        jc.addCommand(CMD_CLEAN_DATA, cleanDataCmd);

        jc.parse(args);

        String command = jc.getParsedCommand();
        if (command == null) {
            jc.usage();
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileReader(commonArgs.getCfg()));

        GeneratorParams params = new GeneratorParams(properties);
        
        DictDataFiles dictdataFiles = new DictDataFiles(params.getDictdataPath());
        DictDataContent dictdataContent = new DictDataContent();
        dictdataContent.loadDictData(dictdataFiles);

        NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();

        DataSource dataSource = DaoUtils.createDataSource("isur", properties);
        DBAppender appender = new DBAppender(dataSource, config);

        Generator generator = new Generator(config, dictdataContent, appender, params);

        switch (command) {
        case CMD_APPEND_DATA:
            generator.appendData();
            break;
        case CMD_CLEAN_DATA:
            generator.cleanData();
            break;
        default:
            break;
        }
    }

}
