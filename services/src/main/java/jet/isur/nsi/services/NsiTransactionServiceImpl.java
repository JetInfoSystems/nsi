package jet.isur.nsi.services;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.codahale.metrics.Timer;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.tx.NsiTransaction;
import jet.isur.nsi.api.tx.NsiTransactionService;
import jet.isur.nsi.common.tx.NsiTransactionImpl;
import jet.scdp.metrics.api.Metrics;
import jet.scdp.metrics.api.MetricsDomain;

@MetricsDomain(name = "nsiTransactionService")
public class NsiTransactionServiceImpl implements NsiTransactionService {

    private DataSource dataSource;
    private final Timer createTransactionTimer;

    public NsiTransactionServiceImpl(Metrics metrics) {
        createTransactionTimer = metrics.timer(getClass(), "createTransaction");
    }


    @Override
    public NsiTransaction createTransaction(String requestId) {
        final Timer.Context t = createTransactionTimer.time();
        try {
            return new NsiTransactionImpl(dataSource.getConnection(), requestId);
        } catch (SQLException e) {
            throw new NsiServiceException("createTransaction error", e);
        } finally {
            t.stop();
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
