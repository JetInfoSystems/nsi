package jet.isur.nsi.migrator.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription="Создать пользователя")
public class CreateUserCmd {
    @Parameter(names = "-tablespace", description="ident табличного пространства по умолчания", required = true)
    private String tablespace;
    
    public String getTablespace() {
        return tablespace;
    }
}
