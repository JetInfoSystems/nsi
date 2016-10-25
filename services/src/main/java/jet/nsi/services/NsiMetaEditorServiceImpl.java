package jet.nsi.services;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;

import jet.metrics.api.Metrics;
import jet.metrics.api.MetricsDomain;
import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.NsiMetaEditorService;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.model.MetaDict;
import jet.nsi.common.helper.MetaDictGen;

@MetricsDomain(name = "nsiMetaService")
public class NsiMetaEditorServiceImpl implements NsiMetaEditorService {

    private static final Logger log = LoggerFactory.getLogger(NsiMetaEditorServiceImpl.class);
    private final Timer metaDictListTimer;
    private final Timer metaDictGetTimer;

    public NsiMetaEditorServiceImpl(Metrics metrics) {
        metaDictListTimer = metrics.timer(getClass(), "metaDictList");
        metaDictGetTimer = metrics.timer(getClass(), "metaDictGet");
    }

    private NsiConfigManager configManager;

    public void setConfigManager(NsiConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Collection<MetaDict> metaDictList(String requestId) {
        final Timer.Context t = metaDictListTimer.time();
        try {
            Collection<MetaDict> dicts = configManager.getConfig().getMetaDicts();
            log.info("metaDictList [{}] -> ok[{}]", requestId, dicts.size());
            return dicts;
        } catch(Exception e) {
            log.error("metaDictList [{}] -> error", requestId, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }

    }

    @Override
    public MetaDict metaDictGet(String requestId, String name) {
        final Timer.Context t = metaDictGetTimer.time();
        try {
            MetaDict dict = configManager.getConfig().getMetaDict(name);
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
    public void metaDictSet(MetaDict metaDict) {
        configManager.writeConfigFile(metaDict);
    }

    @Override
    public MetaDict createMetaDict(String dictName) {
        return MetaDictGen.genMetaDict(dictName).build();
    }

    @Override
    public NsiConfig getConfig() {
        return configManager.getConfig();
    }


}
