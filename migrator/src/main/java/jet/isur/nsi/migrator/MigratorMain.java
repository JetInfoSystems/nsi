package jet.isur.nsi.migrator;

import java.io.FileReader;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import com.beust.jcommander.JCommander;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.migrator.args.CommonArgs;
import jet.isur.nsi.migrator.args.CreateTablespaceCmd;
import jet.isur.nsi.migrator.args.CreateUserCmd;
import jet.isur.nsi.migrator.args.CreateUserProfileCmd;
import jet.isur.nsi.migrator.args.DropTablespaceCmd;
import jet.isur.nsi.migrator.args.DropUserCmd;
import jet.isur.nsi.migrator.args.RollbackCmd;
import jet.isur.nsi.migrator.args.TagCmd;
import jet.isur.nsi.migrator.args.UpdateCmd;
import jet.isur.nsi.testkit.utils.DaoUtils;

public class MigratorMain {

    private static final String IDENT_ISUR = "isur";
    private static final String CMD_UPDATE = "update";
    private static final String CMD_ROLLBACK = "rollback";
    private static final String CMD_TAG = "tag";
    private static final String CMD_CREATE_TABLESPACE = "createTablespace";
    private static final String CMD_DROP_TABLESPACE = "dropTablespace";
    private static final String CMD_CREATE_USER = "createUser";
    private static final String CMD_DROP_USER = "dropUser";
    private static final String CMD_CREATE_USER_PROFILE = "createUserProfile";
    
    public static void main(String[] args) throws Exception {
        JCommander jc = new JCommander();
        CommonArgs commonArgs = new CommonArgs();
        jc.addObject(commonArgs);
        UpdateCmd updateCmd = new UpdateCmd();
        jc.addCommand(CMD_UPDATE, updateCmd);
        RollbackCmd rollbackCmd = new RollbackCmd();
        jc.addCommand(CMD_ROLLBACK, rollbackCmd);
        TagCmd tagCmd = new TagCmd();
        jc.addCommand(CMD_TAG, tagCmd);
        CreateTablespaceCmd createTablespaceCmd = new CreateTablespaceCmd();
        jc.addCommand(CMD_CREATE_TABLESPACE, createTablespaceCmd);
        DropTablespaceCmd dropTablespaceCmd = new DropTablespaceCmd();
        jc.addCommand(CMD_DROP_TABLESPACE, dropTablespaceCmd);
        CreateUserCmd createUserCmd = new CreateUserCmd();
        jc.addCommand(CMD_CREATE_USER, createUserCmd);
        DropUserCmd dropUserCmd = new DropUserCmd();
        jc.addCommand(CMD_DROP_USER, dropUserCmd);
        CreateUserProfileCmd createUserProfileCmd = new CreateUserProfileCmd();
        jc.addCommand(CMD_CREATE_USER_PROFILE, createUserProfileCmd);
        jc.parse(args);

        String command = jc.getParsedCommand();
        if (command == null) {
            jc.usage();
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileReader(commonArgs.getCfg()));

        MigratorParams params = new MigratorParams(properties);

        switch (command) {
        case CMD_UPDATE:
        case CMD_ROLLBACK:
        case CMD_TAG:
        case CMD_CREATE_USER_PROFILE:
        	
            DataSource dataSource = DaoUtils.createDataSource(IDENT_ISUR, properties);

            NsiConfig config = new NsiConfigManagerFactoryImpl().create(params.getMetadataPath()).getConfig();
            Migrator migrator = new Migrator(config, dataSource, params);

            switch (command) {
            case CMD_UPDATE:
                migrator.update(updateCmd.getTag());
                break;
            case CMD_ROLLBACK:
                migrator.update(updateCmd.getTag());
                break;
            case CMD_TAG:
                migrator.update(updateCmd.getTag());
                break;
            case CMD_CREATE_USER_PROFILE:
            	try (Connection con = dataSource.getConnection()) {
            		if(DaoUtils.createUserProfile(con, createUserProfileCmd.getLogin()) == null) {
            			System.out.println("User with dn " +createUserProfileCmd.getLogin()+ " already exists");
            		}
            	}
            	break;
            default:
                break;
            }
            break;
        
        
        	
        case CMD_CREATE_TABLESPACE:
        case CMD_DROP_TABLESPACE:
        case CMD_CREATE_USER:
        case CMD_DROP_USER:

            Connection connection = DaoUtils.createAdminConnection(IDENT_ISUR, properties);

            switch (command) {
            case CMD_CREATE_TABLESPACE:
                DaoUtils.createTablespace(connection,
                        params.getTablespace(IDENT_ISUR),
                        params.getDataFilePath(IDENT_ISUR) + params.getDataFileName(IDENT_ISUR),
                        params.getDataFileSize(IDENT_ISUR),
                        params.getDataFileAutoSize(IDENT_ISUR),
                        params.getDataFileMaxSize(IDENT_ISUR));
                break;
            case CMD_DROP_TABLESPACE:
                DaoUtils.dropTablespace(connection,
                        params.getTablespace(IDENT_ISUR));
                break;
            case CMD_CREATE_USER:
                DaoUtils.createUser(connection,
                        params.getUsername(IDENT_ISUR),
                        params.getPassword(IDENT_ISUR),
                        params.getTablespace(IDENT_ISUR),
                        params.getTempTablespace(IDENT_ISUR));
                break;
            case CMD_DROP_USER:
                DaoUtils.dropUser(connection,
                        params.getUsername(IDENT_ISUR));
                break;
            default:
                break;
            }
            break;

        }

    }

}
