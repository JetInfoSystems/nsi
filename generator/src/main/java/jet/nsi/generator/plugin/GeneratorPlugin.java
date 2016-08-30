package jet.nsi.generator.plugin;

import javax.sql.DataSource;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.generator.GeneratorParams;


public interface GeneratorPlugin {

    void execute(NsiConfig config, DataSource dataSource, GeneratorParams params) throws Exception;

}
