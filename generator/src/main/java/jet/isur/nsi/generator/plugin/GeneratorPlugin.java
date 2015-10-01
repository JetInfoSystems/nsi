package jet.isur.nsi.generator.plugin;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.generator.GeneratorParams;


public interface GeneratorPlugin {

    void execute(NsiConfig config, DataSource dataSource, GeneratorParams params) throws Exception;

}
