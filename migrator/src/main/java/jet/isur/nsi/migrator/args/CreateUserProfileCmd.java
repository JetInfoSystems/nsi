package jet.isur.nsi.migrator.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription="Создание профайла пользователя")
public class CreateUserProfileCmd {
    @Parameter(names = "-login", description="dn пользователя в формате ldap", required = true)
    private String login;
    
    public String getLogin() {
    	return login;
    }
}
