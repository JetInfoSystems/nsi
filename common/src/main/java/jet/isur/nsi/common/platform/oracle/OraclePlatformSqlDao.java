package jet.isur.nsi.common.platform.oracle;

import java.sql.Connection;
import java.sql.SQLSyntaxErrorException;

import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultDataType;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.OperationType;
import jet.isur.nsi.api.platform.NsiPlatform;
import jet.isur.nsi.common.platform.DefaultPlatformSqlDao;

public class OraclePlatformSqlDao extends DefaultPlatformSqlDao {

    
    public OraclePlatformSqlDao(NsiPlatform nsiPlatform) {
        super(nsiPlatform);
    }

    @Override
    public String wrapFilterFieldValue(BoolExp filter, NsiConfigField field,
            String val) {
        if (filter.getFunc().equals(OperationType.CONTAINS)) {
            return "%" + val + "%";
        } else {
            return super.wrapFilterFieldValue(filter, field, val);
        }
    }

    @Override
    public void createTable(NsiConfigDict dict, Connection connection) {
        CreateTableAsStep<?> createTableAsStep = getQueryBuilder(connection).createTable(dict.getTable());
        CreateTableColumnStep createTableColumnStep = null;
        for (NsiConfigField field : dict.getFields()) {
            createTableColumnStep = createTableAsStep.column(field.getName(), getDataType(field.getType())
                    .length(field.getSize()).precision(field.getSize(),field.getPrecision()));
        }
        if(createTableColumnStep != null) {
            createTableColumnStep.execute();
        } else {
            throw new NsiServiceException("no fields found");
        }
    }

    @Override
    public void dropTable(NsiConfigDict dict, Connection connection) {
        dropTable(dict.getTable(), connection);
    }

    @Override
    public void dropTable(String name, Connection connection) {
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

    @Override
    public void createSeq(NsiConfigDict dict, Connection connection) {
        getQueryBuilder(connection).createSequence(dict.getSeq()).execute();
    }

    @Override
    public void dropSeq(NsiConfigDict dict, Connection connection) {
        dropSeq(dict.getSeq(), connection);
    }

    @Override
    public void dropSeq(String name, Connection connection) {
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

    @Override
    public DataType<?> getDataType(MetaFieldType fieldType) {
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
        case CLOB:
            type = "clob";
            break;
        default:
            throw new NsiServiceException("unsupported field type: " + fieldType);
        }
        return DefaultDataType.getDataType(SQLDialect.DEFAULT, type);
    }

    @Override
    public void createFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        String table = dict.getTable();
        executeSql(connection, new StringBuilder()
                .append("CREATE INDEX ")
                .append("fti_").append(table).append("_").append(field)
                .append(" ON ")
                .append(table)
                .append("(").append(field).append(")")
                .append(" INDEXTYPE IS CTXSYS.CONTEXT ")
                .append("PARAMETERS ('filter ctxsys.null_filter lexer isur sync(on commit)')")
                .toString());
    }

    @Override
    public void dropFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        executeSql(connection, new StringBuilder()
                .append("DROP INDEX ")
                .append("fti_").append(dict.getTable()).append("_").append(field).toString());
    }

    @Override
    public void recreateFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        try {
            createFullSearchIndex(dict, field, connection);
        }
        catch(Exception e) {
            dropFullSearchIndex(dict, field, connection);
            createFullSearchIndex(dict, field, connection);
        }
    }


}
