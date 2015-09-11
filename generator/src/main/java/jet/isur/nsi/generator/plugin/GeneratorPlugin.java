package jet.isur.nsi.generator.plugin;

import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;


public interface GeneratorPlugin {

    void execute(NsiConfig config, DataSource dataSource, Properties properties) throws Exception;

}
