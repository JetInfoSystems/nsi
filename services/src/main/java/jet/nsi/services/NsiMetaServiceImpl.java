package jet.nsi.services;

import java.util.Collection;

import jet.metrics.api.Metrics;
import jet.metrics.api.MetricsDomain;
import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.NsiMetaService;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;

@MetricsDomain(name = "nsiMetaService")
public class NsiMetaServiceImpl implements NsiMetaService {

    private static final Logger log = LoggerFactory.getLogger(NsiMetaServiceImpl.class);
    private final Timer metaDictListTimer;
    private final Timer metaDictGetTimer;

    public NsiMetaServiceImpl(Metrics metrics) {
        metaDictListTimer = metrics.timer(getClass(), "metaDictList");
        metaDictGetTimer = metrics.timer(getClass(), "metaDictGet");
    }

    private NsiConfigManager configManager;

    public void setConfigManager(NsiConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Collection<NsiConfigDict> metaDictList(String requestId) {
        final Timer.Context t = metaDictListTimer.time();
        try {
            Collection<NsiConfigDict> dicts = configManager.getConfig().getDicts();
            log.info("metaDictList [{}] -> ok [{}]", requestId, dicts.size());
            return dicts;
        } catch(Exception e) {
            log.error("metaDictList [{}] -> error", requestId, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }

    }

    @Override
    public NsiConfigDict metaDictGet(String requestId, String name) {
        final Timer.Context t = metaDictGetTimer.time();
        try {
            NsiConfigDict dict = configManager.getConfig().getDict(name);
            log.info("metaDictGet [{},{}] -> ok", requestId, name);
            return dict;
        } catch (Exception e) {
            log.error("metaDictGet [{},{}] -> error", requestId, name, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public NsiConfig getConfig() {
        return configManager.getConfig();
    }


}