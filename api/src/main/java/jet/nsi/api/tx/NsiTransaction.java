package jet.nsi.api.tx;

import java.sql.Connection;

public interface NsiTransaction extends AutoCloseable {

    String getRequestId();

    Connection getConnection();

    void rollback();
}
