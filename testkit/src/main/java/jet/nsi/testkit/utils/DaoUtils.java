package jet.nsi.testkit.utils;

import java.util.Properties;

import javax.sql.DataSource;

import com.jolbox.bonecp.BoneCPDataSource;

public class DaoUtils {

    public static DataSource createDataSource(String name, Properties properties) {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
        dataSource.setJdbcUrl(properties.getProperty("db." + name + ".url"));
        dataSource.setUsername(properties.getProperty("db." + name + ".username"));
        dataSource.setPassword(properties.getProperty("db." + name + ".password"));
        dataSource.setConnectionTimeoutInMs(15000);
        //dataSource.setConnectionTestStatement("select 1 from dual");
        dataSource.setMaxConnectionsPerPartition(Integer.parseInt(properties.getProperty("db." + name + ".size", "20")));
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }



}
