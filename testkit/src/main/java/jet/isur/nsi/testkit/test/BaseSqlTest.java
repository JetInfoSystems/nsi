package jet.isur.nsi.testkit.test;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.testkit.utils.DaoUtils;

import org.junit.After;
import org.junit.Before;

public class BaseSqlTest {
    protected DataSource dataSource;
    protected Properties properties;

    @Before
    public void setupInternal() throws Exception {
        setup();
    }

    public void setup() throws Exception {
        properties = new Properties();
        File file = new File("target/test-classes/project.properties").getAbsoluteFile();
        try(FileReader reader = new FileReader(file)) {
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

    protected String getProperty(String key,String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
