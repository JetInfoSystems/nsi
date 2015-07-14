package jet.isur.nsi.generator;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiLocalGitConfigManagerImpl;
import jet.isur.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;
import jet.isur.nsi.testkit.utils.DaoUtils;

import org.junit.After;
import org.junit.Before;

public class BaseTest {
    protected NsiConfig config;
    protected DataSource dataSource;

    @Before
    public void setupInternal() throws Exception {
        setup();
    }

    public void setup() throws Exception {
        String configPath = "src/test/resources/metadata";
        NsiLocalGitConfigManagerImpl manager = new NsiLocalGitConfigManagerImpl(new File(configPath), new NsiYamlMetaDictReaderImpl());
        config = manager.getConfig();

        Properties properties = new Properties();
        try(FileReader reader = new FileReader("target/test-classes/project.properties")) {
            properties.load(reader);
        }
        dataSource = DaoUtils.createDataSource("isur", properties);
    }

    @After
    public void cleanupInternal() {
        cleanup();
    }

    public void cleanup() {
    }
}
