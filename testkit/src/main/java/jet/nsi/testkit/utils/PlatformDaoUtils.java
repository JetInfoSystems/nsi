package jet.nsi.testkit.utils;

import java.util.Properties;

import javax.sql.DataSource;

public interface PlatformDaoUtils {

    DataSource createDataSource(String name, Properties properties);

}
