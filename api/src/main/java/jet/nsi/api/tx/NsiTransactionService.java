package jet.nsi.api.tx;

import java.sql.Connection;

/**
 * Сервис предназначен для управления транзакциями связанными с одним источником данных
 */
public interface NsiTransactionService {

    /**
     * Метод создает объект для представления транзакции, который будет использоваться
     * для объединения нескольких вызовов в одну транзакцию.
     * @return
     */
    NsiTransaction createTransaction(String requestId, Connection connection);
}
