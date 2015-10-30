package jet.isur.nsi.api.tx;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Сессия работы с НСИ (читай БД). При работе внутри НСИ транзакции {@link NsiTransaction} сессия использует коннекшн
 * транзакции и при закрытии сессии ничего не делает с коннекшеном. При работе вне НСИ транзакции сессия создает
 * отдельный коннекшн и делает {@link Connection#commit()} и {@link Connection#close()} при закрытии сессии.
 */
public class NsiSession implements AutoCloseable {

    private final NsiTransaction tx;
    private Connection connection;

    /**
     * Открывает сессию, используя коннекшн из транзакции, если она имеется, либо создавая новый
     */
    public NsiSession(DataSource dataSource) throws SQLException {
        tx = NsiTransaction.get();
        if (tx != null) {
            connection = tx.getConnection();
        } else {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        }
    }

    /**
     * Закрывает сессиию, а также коннекшн вместе с ней, если он был создан только в рамках текущей сессии.
     * В противном случае забота о закрытии коннекшна (транзакции) лежит на инициаторе транзакции.
     */
    @Override
    public void close() throws SQLException {
        if(tx == null && connection != null) {
            connection.commit();
            connection.close();
        }
        connection = null;  // чтобы все последующие закрытия сессии отрабатывали в холостую
    }

    /**
     * Возвращает ассоциированный с сессией коннекшн к БД
     */
    public Connection getConnection() {
        return connection;
    }
}
