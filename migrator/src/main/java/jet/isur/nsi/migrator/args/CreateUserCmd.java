package jet.isur.nsi.migrator.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription="Создать пользователя")
public class CreateUserCmd {
    @Parameter(names = "-tablespaceIdent", description="ident табличного пространства по умолчания", required = true)
    private String tablespaceIdent;
    
    public String getTablespaceIdent() {
        return tablespaceIdent;
    }
}
