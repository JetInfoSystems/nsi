package jet.isur.nsi.api.tx;

import javax.sql.DataSource;

/**
 * Сервис, способный начинать НСИ транзакцию {@link NsiTransaction}
 */
public interface TransactionalService {

    /**
     * Начинает транзакцию в текущем потоке и возвращает ее.
     * @see NsiTransaction#begin(DataSource)
     */
    NsiTransaction beginTransaction();
}
