package jet.nsi.services;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;

import jet.metrics.api.Metrics;
import jet.metrics.api.MetricsDomain;
import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.NsiMetaService;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;

@MetricsDomain(name = "nsiMetaService")
public class NsiMetaServiceImpl implements NsiMetaService {

    private static final Logger log = LoggerFactory.getLogger(NsiMetaServiceImpl.class);
    private final Timer metaDictListTimer;
    private final Timer metaDictGetTimer;
    private final Timer reloadConfigTimer;
    private final Timer copyConfigTimer;

    public NsiMetaServiceImpl(Metrics metrics) {
        metaDictListTimer = metrics.timer(getClass(), "metaDictList");
        metaDictGetTimer = metrics.timer(getClass(), "metaDictGet");
        reloadConfigTimer = metrics.timer(getClass(), "reloadConfigTimer");
        copyConfigTimer = metrics.timer(getClass(), "copyConfigTimer");
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
    public Collection<NsiConfigDict> metaDictList(String requestId, Collection<String> labels) {
        final Timer.Context t = metaDictListTimer.time();
        try {
            Collection<NsiConfigDict> dicts = configManager.getConfig().getDicts(labels);
            log.info("metaDictList [{}, {}] -> ok [{}]", requestId, labels, dicts.size());
            return dicts;
        } catch(Exception e) {
            log.error("metaDictList [{}, {}] -> error", requestId, labels, e);
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
    public NsiConfig reloadConfig(String requestId) {
        final Timer.Context t = reloadConfigTimer.time();
        try {
            configManager.reloadConfig();
            log.info("reloadConfig[{}] -> ok", requestId);
        } catch (Exception e) {
            log.error("reloadConfig [{},{}] -> error", requestId, e);
        } finally {
            t.stop();
        }
        
        return configManager.getConfig();
    }
    
    @Override
    public void checkoutNewConfig(String requestId, String from) {
        final Timer.Context t = copyConfigTimer.time();
        try {
            configManager.checkoutNewConfig(from);
            log.info("checkoutNewConfig [{}, {}] -> ok", requestId, from);
        } catch (Exception e) {
            log.error("checkoutNewConfig [{},{}] -> error", requestId, e);
        } finally {
            t.stop();
        }
    }

    @Override
    public NsiConfig getConfig() {
        return configManager.getConfig();
    }

    

}
