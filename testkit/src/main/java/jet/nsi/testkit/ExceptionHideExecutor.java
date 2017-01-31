package jet.nsi.testkit;

import jet.nsi.api.data.NsiConfigDict;

import java.sql.Connection;

public interface ExceptionHideExecutor {
    void execute(NsiConfigDict dict, Connection connection) throws Exception;

}
