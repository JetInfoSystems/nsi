package jet.isur.nsi.common.sql.test;

import java.io.FileReader;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiLocalGitConfigManagerImpl;
import jet.isur.nsi.common.sql.SqlGen;
import jet.isur.nsi.common.utils.ConfigUtils;
import jet.isur.nsi.common.utils.DaoUtils;

import org.junit.After;
import org.junit.Before;

public class BaseSqlTest {
    protected NsiConfig config;
    protected SqlGen sqlGen;
    protected DataSource dataSource;

    @Before
    public void setupInternal() throws Exception {
        setup();
    }

    public void setup() throws Exception {
        NsiLocalGitConfigManagerImpl manager = ConfigUtils.buildConfigManager("src/test/resources/metadata1");
        config = manager.getConfig();
        sqlGen = new SqlGen();

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
