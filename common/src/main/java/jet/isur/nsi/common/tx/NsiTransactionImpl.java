package jet.isur.nsi.common.tx;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.common.base.Preconditions;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.tx.NsiTransaction;

public class NsiTransactionImpl implements NsiTransaction {

    private final String requestId;
    private final Connection connection;
    private final boolean autoCommit;
    private boolean rolledBack;

    public NsiTransactionImpl(Connection connection, String requestId) throws SQLException {
        Preconditions.checkNotNull(connection, "connection required");
        this.requestId = requestId;
        this.rolledBack = false;
        this.connection = connection;
        this.autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
    }

    @Override
    public void rollback() {
        try {
            // метим, чтобы при закрытии транзакции не делать commit
            rolledBack = true;
            connection.rollback();
        } catch (Exception e) {
            throw new NsiServiceException("Failed to close transaction", e);
        }
    }

    @Override
    public void close() {
        try {
            try {
                if(!rolledBack) {
                    connection.commit();
                }
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (Exception e) {
            throw new NsiServiceException("Failed to restore transaction autoCommit", e);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                throw new NsiServiceException("Failed to close transaction", e);
            }
        }
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
