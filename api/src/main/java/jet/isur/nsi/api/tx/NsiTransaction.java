package jet.isur.nsi.api.tx;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Транзакция работы с НСИ (читай БД). Транзакция закрепляется за текущим потоком с помощью {@link ThreadLocal}.
 * Транзакцию следует закрывать вручную с помощью метода {@link #close()}. В частности это можно делать, используя
 * конструкцию <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources</a>
 */
public class NsiTransaction implements AutoCloseable {

    private static final ThreadLocal<NsiTransaction> instance = new ThreadLocal<>();
    private boolean originAutoCommit;
    private final Connection connection;
    private boolean rolledBack;

    public NsiTransaction(Connection connection) {
        this.connection = connection;
        this.rolledBack = false;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Начинает транзакцию в текущем потоке и возвращает ее.
     * @throws NsiTransactionException
     */
    public static NsiTransaction begin(DataSource dataSource) throws NsiTransactionException {
        try {
            final Connection connection = dataSource.getConnection();
            final NsiTransaction tx = new NsiTransaction(connection);
            tx.originAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            instance.set(tx);
            return tx;
        } catch (Exception ex) {
            throw new NsiTransactionException("Failed to begin transaction", ex);
        }
    }

    /**
     * Возвращает транзакцию текущего потока
     */
    public static NsiTransaction get() {
        return instance.get();
    }

    /**
     * Откатывает транзакцию. При этом откатятся изменения в БД.
     */
    public void rollback() {
        try {
            rolledBack = true;      // метим, чтобы при закрытии транзакции не делать commit
            if (connection == null) {
                throw new NsiTransactionException("connection not found");
            }
            connection.rollback();
        } catch (Exception ex) {
            // игнорим
        } finally {
            instance.set(null);
        }
    }

    /**
     * Закрывает транзакцию, выполняя commit изменений в БД.
     * @throws NsiTransactionException
     */
    @Override
    public void close() throws NsiTransactionException {
        try {
            if (connection == null) {
                throw new NsiTransactionException("connection not found");
            }
            if(!rolledBack) {
                connection.commit();
            }
            connection.setAutoCommit(originAutoCommit);
            connection.close();
        } catch (Exception ex) {
            throw new NsiTransactionException("Failed to close transaction", ex);
        } finally {
            instance.set(null);
        }
    }
}
