package liquibase.sqlgenerator.ext;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.statement.core.InsertStatement;

/**
 * Created by kazantsev on 13.01.17.
 */
public class PhoenixInsertGenerator extends InsertGenerator {


    @Override
    public void generateHeader(StringBuffer sql, InsertStatement statement, Database database) {
        sql.append("UPSERT INTO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " (");
        for (String column : statement.getColumnValues().keySet()) {
            sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(", ");
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }

        sql.append(") VALUES ");
    }


    @Override
    public boolean supports(InsertStatement statement, Database database) {
//        return database instanceof PhoenixJdbcDatabase;
        return true;
    }

    @Override
    public int getPriority() {
        return 5;
    }

}
