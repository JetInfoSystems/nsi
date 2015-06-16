package jet.isur.nsi.common.utils;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.common.data.NsiDataException;

import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;

import com.jolbox.bonecp.BoneCPDataSource;

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
            throw new NsiDataException("no fields found");
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
        getQueryBuilder(connection).dropTable(name).execute();
    }

    public static void createSeq(NsiConfigDict dict, Connection connection) {
        createSeq(dict.getSeq(),connection);
    }

    public static void createSeq(String name, Connection connection) {
        getQueryBuilder(connection).createSequence(name).execute();
    }

    public static void dropSeq(NsiConfigDict dict, Connection connection) {
        dropSeq(dict.getSeq(), connection);
    }

    public static void dropSeq(String name, Connection connection) {
        getQueryBuilder(connection).dropSequence(name).execute();
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
            throw new NsiDataException("unsupported field type: " + fieldType);
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
}
