package jet.nsi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;

import jet.metrics.api.Metrics;
import jet.nsi.api.NsiMigratorService;
import jet.nsi.api.NsiServiceException;
import jet.nsi.migrator.Migrator;

public class NsiMigratorServiceImpl implements NsiMigratorService {
    private static final Logger log = LoggerFactory.getLogger(NsiMigratorServiceImpl.class);

    private final Timer updateTimer;

    public Migrator migrator;

    public NsiMigratorServiceImpl(Metrics metrics) {
        updateTimer = metrics.timer(getClass(), "updateTimer");
    }

    @Override
    public void update(String requestId, String tag) {
        final Timer.Context t = updateTimer.time();
        try {
            // migrator.update(tag);
            log.info("update [{}] -> done", requestId);
        } catch (Exception e) {
            log.error("update [{}] -> error", requestId, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }

    }

    public void setMigrator(Migrator migrator) {
        this.migrator = migrator;
    }
}
