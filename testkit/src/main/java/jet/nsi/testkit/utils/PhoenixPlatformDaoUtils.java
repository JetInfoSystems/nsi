package jet.nsi.testkit.utils;

import com.jolbox.bonecp.BoneCPDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class PhoenixPlatformDaoUtils implements PlatformDaoUtils {

    @Override
    public DataSource createDataSource(String name, Properties properties) {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("org.apache.phoenix.jdbc.PhoenixDriver");
        dataSource.setJdbcUrl(properties.getProperty("db." + name + ".url"));
        dataSource.setConnectionTestStatement("select 1");
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }
}
