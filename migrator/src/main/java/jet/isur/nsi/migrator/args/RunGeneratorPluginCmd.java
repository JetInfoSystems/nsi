package jet.isur.nsi.migrator.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription="Запустить генератор")
public class RunGeneratorPluginCmd {
    @Parameter(names = "-pluginClass", description="Класс плагина", required = true)
    private String pluginClass;

    public String getPluginClass() {
        return pluginClass;
    }


}
