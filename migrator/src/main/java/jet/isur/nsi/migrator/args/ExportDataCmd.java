package jet.isur.nsi.migrator.args;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

@Parameters(separators = "=", commandDescription="Выгрузить данные")
public class ExportDataCmd {
    @Parameter(names = "-outFile", converter = FileConverter.class, description="Выходной файл", required = true)
    private File outFile;
    
    @Parameter(names = "-dictName", description="Справочник", required = true)
    private String dictName;

    @Parameter(names = "-includeOwned", description="Выгрузить подчиненные данные", required = false)
    private boolean includeOwned = true;

    public File getOutFile() {
        return outFile;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public boolean isIncludeOwned() {
        return includeOwned;
    }

    public void setIncludeOwned(boolean includeOwned) {
        this.includeOwned = includeOwned;
    }

    
}
