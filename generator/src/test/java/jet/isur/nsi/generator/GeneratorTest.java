package jet.isur.nsi.generator;

import java.io.File;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;

import org.junit.Test;

public class GeneratorTest extends BaseTest{

    private NsiConfig config;
    private Generator generator;

    @Override
    public void setup() throws Exception {
        super.setup();

        File configPath = new File("/opt/isur/database/metadata");
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
        DBAppender appender = new DBAppender(dataSource, config);
        generator = new Generator(config, appender );
    }

    @Test
    public void testAppendWORK_TYPE() {
        generator.addData("WORK_TYPE");
        generator.appendData();
    }

    @Test
    public void testAppendEMP() {
        generator.addData("EMP");
        generator.appendData();
    }

    @Test
    public void testAppendEVENT() {
        generator.addData("EVENT", 100);
        generator.appendData();
    }

    @Test
    public void testAppendMSG() {
        generator.addData("MSG");
        generator.appendData();
    }


    @Test
    public void testCleanDatabase() {
        /*
        generator.cleanDatabase();
        */
    }
}
