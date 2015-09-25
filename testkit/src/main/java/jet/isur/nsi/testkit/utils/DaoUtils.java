package jet.isur.nsi.testkit.utils;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Properties;

import javax.sql.DataSource;

import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;

import com.jolbox.bonecp.BoneCPDataSource;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.model.MetaFieldType;

public class DaoUtils {

    // jooq hack
    private static DefaultDataType<String> VARCHAR2 = new DefaultDataType<String>(SQLDialect.DEFAULT,
            new DefaultDataType<String>(null, String.class, "varchar2"), "varchar2");
    private static DefaultDataType<String> NUMBER = new DefaultDataType<String>(SQLDialect.DEFAULT,
            new DefaultDataType<String>(null, String.class, "number"), "number");

    public static void createTable(NsiConfigDict dict, Connection connection) {

        CreateTableAsStep<?> createTableAsStep = getQueryBuilder(connection).createTable(dict.getTable());
        CreateTableColumnStep createTableColumnStep = null;
        for (NsiConfigField field : dict.getFields()) {
            createTableColumnStep = createTableAsStep.column(field.getName(), getDataType(field.getType())
                    .length(field.getSize()).precision(field.getPrecision()));
        }
        if(createTableColumnStep != null) {
            createTableColumnStep.execute();
        } else {
            throw new NsiServiceException("no fields found");
        }
    }

    public static void recreateTable(NsiConfigDict dict, Connection connection) {
        try {
            createTable(dict, connection);
        }
        catch(Exception e) {
            dropTable(dict, connection);
            createTable(dict, connection);
        }

    }

	public static DSLContext getQueryBuilder(Connection connection) {
        Settings settings = new Settings();
        settings.setRenderNameStyle(RenderNameStyle.UPPER);
        return DSL.using(connection,SQLDialect.DEFAULT,settings );
    }

    public static void dropTable(NsiConfigDict dict, Connection connection) {
        dropTable(dict.getTable(), connection);
    }

    public static void dropTable(String name, Connection connection) {
        try {
            getQueryBuilder(connection).dropTable(name).execute();
        }
        catch(DataAccessException e) {
            Throwable cause = e.getCause();
            if(cause instanceof SQLSyntaxErrorException) {
                throwIfNot((SQLSyntaxErrorException)cause, 942);
            } else {
                throw e;
            }
        }
    }

    private static void throwIfNot(SQLSyntaxErrorException e, int errorCode) {
        if(e.getErrorCode() != errorCode) {
            throw new RuntimeException(e);
        }
    }

    public static void createSeq(NsiConfigDict dict, Connection connection) {
        createSeq(dict.getSeq(),connection);
    }

    public static void createSeq(String name, Connection connection) {
        getQueryBuilder(connection).createSequence(name).execute();
    }

    public static void recreateSeq(NsiConfigDict dict, Connection connection) {
        try {
            createSeq(dict, connection);
        }
        catch (Exception e) {
            dropSeq(dict,connection);
            createSeq(dict, connection);
        }
    }


    public static void dropSeq(NsiConfigDict dict, Connection connection) {
        dropSeq(dict.getSeq(), connection);
    }

    public static void dropSeq(String name, Connection connection) {
        try {
            getQueryBuilder(connection).dropSequence(name).execute();
        }
        catch(DataAccessException e) {
            Throwable cause = e.getCause();
            if(cause instanceof SQLSyntaxErrorException) {
                throwIfNot((SQLSyntaxErrorException)cause, 2289);
            } else {
                throw e;
            }
        }
    }

    public static void executeSql(Connection connection, String sql) {
        try( PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("execute: " + sql, e);
        }
    }

    public static DataType<?> getDataType(MetaFieldType fieldType) {
        String type = null;
        switch (fieldType) {
        case BOOLEAN:
            type = "char";
            break;
        case DATE_TIME:
            type = "date";
            break;
        case NUMBER:
            type = "number";
            break;
        case VARCHAR:
            type = "varchar2";
            break;
        case CHAR:
            type = "char";
            break;
        default:
            throw new NsiServiceException("unsupported field type: " + fieldType);
        }
        return DefaultDataType.getDataType(SQLDialect.DEFAULT, type);
    }

    public static DataSource createDataSource(String name, Properties properties) {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
        dataSource.setJdbcUrl(properties.getProperty("db." + name + ".url"));
        dataSource.setUsername(properties.getProperty("db." + name + ".username"));
        dataSource.setPassword(properties.getProperty("db." + name + ".password"));
        dataSource.setConnectionTimeoutInMs(15000);
        dataSource.setConnectionTestStatement("select 1 from dual");
        dataSource.setMaxConnectionsPerPartition(Integer.parseInt(properties.getProperty("db." + name + ".size", "20")));
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }

    public static Connection createAdminConnection(String name, Properties properties) throws SQLException {
        Properties connectProperties = new Properties();
        connectProperties.put("user", properties.getProperty("db." + name + ".sys.username") + " as sysdba");
        connectProperties.put("password", properties.getProperty("db." + name + ".sys.password"));
        return DriverManager.getConnection (properties.getProperty("db." + name + ".url"), connectProperties );
    }

    public static void createTablespace(Connection connection, String name,String dataFileName,
            String dataFileSize, String dataFileAutoSize, String dataFileMaxSize) throws SQLException {
        executeSql(connection, new StringBuilder()
            .append(" create tablespace ").append(name)
            .append(" datafile '").append(dataFileName).append("' ")
            .append(" size ").append(dataFileSize).append(" reuse ")
            .append(" autoextend on next ").append(dataFileAutoSize)
            .append(" maxsize ").append(dataFileMaxSize).toString());
    }

    public static void dropTablespace(Connection connection, String name) throws SQLException {
        executeSql(connection, new StringBuilder()
            .append(" drop tablespace ").append(name).append(" including contents and datafiles").toString());
    }

    public static void createUser(Connection connection, String name,String password,
            String defaultTablespace, String tempTablespace) throws SQLException {
        executeSql(connection, new StringBuilder()
            .append(" create user ").append(name)
            .append(" IDENTIFIED BY \"").append(password).append("\" ")
            .append(" DEFAULT TABLESPACE ").append(defaultTablespace)
            .append(" TEMPORARY TABLESPACE ").append(tempTablespace).toString());
        executeSql(connection, new StringBuilder()
            .append(" ALTER USER ").append(name)
            .append(" QUOTA UNLIMITED ON ").append(defaultTablespace).toString());
        executeSql(connection, new StringBuilder().append(" GRANT RESOURCE TO ").append(name).toString());
        executeSql(connection, new StringBuilder().append(" GRANT CONNECT TO ").append(name).toString());
        executeSql(connection, new StringBuilder().append(" GRANT CREATE ANY VIEW TO ").append(name).toString());
    }

    public static void dropUser(Connection connection, String name) throws SQLException {
        executeSql(connection, new StringBuilder()
            .append(" DROP USER  ").append(name).append(" CASCADE").toString());
    }

	public static void removeUserProfile(Connection con, Long id) throws SQLException {
		DSLContext dsl = getQueryBuilder(con);
		dsl.delete(table("USER_PROFILE").as("u")).where(field("u.id").eq(id)).execute();
	}
	 
	public static int countUserProfile(Connection con, String login) throws SQLException {
		DSLContext dsl = getQueryBuilder(con);
		return dsl.selectCount().from(table("USER_PROFILE").as("u"))
			.where(field("u.login").eq(login)).fetchOne(0, int.class);
	}
	
	public static Long createUserProfile(Connection con, String login) throws SQLException {
		int count = countUserProfile(con, login);
		
		if(count > 0) {
			return null;
		}
		
		DSLContext dsl = getQueryBuilder(con);
		Long id = dsl.nextval("SEQ_USER_PROFILE").longValue();
		dsl.insertInto(table("USER_PROFILE").as("u"), 
			field("u.id"), field("u.IS_DELETED"), field("u.LOGIN"), field("u.STATE"))
			.values(id, "N", login, "1")
			.execute();
		
		return id;
	}
		
		
}
