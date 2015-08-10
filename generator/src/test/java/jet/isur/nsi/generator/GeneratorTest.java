package jet.isur.nsi.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.testkit.test.BaseSqlTest;

import org.junit.Test;

public class GeneratorTest extends BaseSqlTest{

    private NsiConfig config;
    private Generator generator;
	private String metadataPath;

    @Override
    public void setup() throws Exception {
        super.setup();

        getConfiguration();
        
        File configPath = new File(metadataPath);
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
        DBAppender appender = new DBAppender(dataSource, config);
        //generator = new Generator(config, appender );
    }

    @Test
    public void testAppendWORK_TYPE() {
        //generator.addData("WORK_TYPE");
        //generator.appendData();
    }

    @Test
    public void testAppendEMP() {
        //generator.addData("EMP");
        //generator.appendData();
    }

    @Test
    public void testAppendEVENT() {
        //generator.addData("EVENT", 100);
        //generator.appendData();
    }

    @Test
    public void testAppendMSG() {
        //generator.addData("MSG");
        //generator.appendData();
    }


    @Test
    public void testCleanDatabase() {
        /*
        generator.cleanDatabase();
        */
    }

    private void getConfiguration() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("project.properties");
        Properties props = new Properties();
        props.load(in);
        metadataPath = props.getProperty("database.metadata.path", "/opt/isur/database/metadata");
    }
}
