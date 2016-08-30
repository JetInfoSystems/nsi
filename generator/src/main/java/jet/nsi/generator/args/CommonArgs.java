package jet.nsi.generator.args;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

@Parameters(separators = "=")
public class CommonArgs {
    public final static File DEFAULT_CFG = new File("default.properties");

    @Parameter(names = "-cfg", converter = FileConverter.class, description="Файл конфигурации", required = false)
    private File cfg = DEFAULT_CFG;

    public File getCfg() {
        return cfg;
    }

    public void setCfg(File cfg) {
        this.cfg = cfg;
    }

}