package jet.isur.nsi.migrator.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription="Создать табличное пространство")
public class CreateTablespaceCmd {
    @Parameter(names = "-ident", description="ident табличного пространства", required = true)
    private String ident;
    
    public String getIdent() {
        return ident;
    }
}
