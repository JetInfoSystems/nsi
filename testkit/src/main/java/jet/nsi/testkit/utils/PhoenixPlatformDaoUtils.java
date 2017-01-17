package jet.nsi.testkit.utils;

import com.jolbox.bonecp.BoneCPDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class PhoenixPlatformDaoUtils implements PlatformDaoUtils {

    @Override
    public DataSource createDataSource(String name, Properties properties) {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("org.apache.phoenix.jdbc.PhoenixDriver");
//        dataSource.setDriverClass("org.postgresql.Driver");
        dataSource.setJdbcUrl(properties.getProperty("db." + name + ".url"));
//        dataSource.setUsername(properties.getProperty("db." + name + ".username"));
//        dataSource.setPassword(properties.getProperty("db." + name + ".password"));
//        dataSource.setConnectionTimeoutInMs(15000);
        dataSource.setConnectionTestStatement("select 1");
//        dataSource.setMaxConnectionsPerPartition(Integer.parseInt(properties.getProperty("db." + name + ".size", "20")));
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }



}
