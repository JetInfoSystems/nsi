package jet.isur.nsi.migrator.args;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

@Parameters(separators = "=")
public class CommonArgs {
    @Parameter(names = "-cfg", converter = FileConverter.class, description="Файл конфигурации", required = true)
    private File cfg;

    public File getCfg() {
        return cfg;
    }

    public void setCfg(File cfg) {
        this.cfg = cfg;
    }

}