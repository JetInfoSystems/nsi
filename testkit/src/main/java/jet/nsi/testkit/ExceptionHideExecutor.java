package jet.nsi.testkit;

import jet.nsi.api.data.NsiConfigDict;

import java.sql.Connection;

/**
 * Created by kazantsev on 25.01.17.
 */
//@FunctionalInterface
public interface ExceptionHideExecutor {
    void execute(NsiConfigDict dict, Connection connection) throws Exception;

}
