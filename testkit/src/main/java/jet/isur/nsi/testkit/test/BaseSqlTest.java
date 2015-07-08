package jet.isur.nsi.testkit.test;

import java.io.FileReader;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.testkit.utils.DaoUtils;

import org.junit.After;
import org.junit.Before;

public class BaseSqlTest {
    protected DataSource dataSource;

    @Before
    public void setupInternal() throws Exception {
        setup();
    }

    public void setup() throws Exception {
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
