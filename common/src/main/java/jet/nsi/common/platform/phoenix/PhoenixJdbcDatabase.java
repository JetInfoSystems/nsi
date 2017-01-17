package jet.nsi.common.platform.phoenix;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

/**
 * Created by kazantsev on 13.01.17.
 */
public class PhoenixJdbcDatabase extends AbstractJdbcDatabase {
    @Override
    public String getCurrentDateTimeFunction() {
        return "current_time()";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return null;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return false;
    }

    @Override
    public String getDefaultDriver(String url) {
        return null;
    }

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public Integer getDefaultPort() {
        return 0;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
