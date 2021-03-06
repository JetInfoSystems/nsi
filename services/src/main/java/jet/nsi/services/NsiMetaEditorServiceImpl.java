package jet.nsi.services;

import java.util.ArrayList;
import java.util.Collection;

import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.model.MetaAttr;
import jet.nsi.api.model.MetaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import jet.metrics.api.Metrics;
import jet.metrics.api.MetricsDomain;
import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.NsiMetaEditorService;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.model.MetaDict;

@MetricsDomain(name = "nsiMetaService")
public class NsiMetaEditorServiceImpl implements NsiMetaEditorService {

    private static final Logger log = LoggerFactory.getLogger(NsiMetaEditorServiceImpl.class);
    private final Timer metaDictListTimer;
    private final Timer metaDictGetTimer;
    private final Timer metaDictCreateTimer;
    private final Timer metaDictSetTimer;

    public NsiMetaEditorServiceImpl(Metrics metrics) {
        metaDictListTimer = metrics.timer(getClass(), "metaDictList");
        metaDictGetTimer = metrics.timer(getClass(), "metaDictGet");
        metaDictCreateTimer = metrics.timer(getClass(), "metaDictCreate");
        metaDictSetTimer = metrics.timer(getClass(), "metaDictSet");
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
    public MetaDict metaDictCreate(String requestId, MetaDict metaDict) {
        final Timer.Context t = metaDictCreateTimer.time();
        try {
            Preconditions.checkNotNull(metaDict, "metaDict is not set");
            Preconditions.checkArgument(configManager.getConfig().getDict(metaDict.getName()) == null, "dict already exists");

            MetaDict newDict = configManager.getConfig().addDictNew(metaDict);
            configManager.getConfig().postCheck();

            log.info("metaDictCreate [{},{}] -> ok", requestId, metaDict.getName());
            return newDict;
        } catch (Exception e) {
            log.error("metaDictCreate [{},{}] -> error", requestId, metaDict.getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public MetaDict metaDictSet(String requestId, MetaDict metaDict) {
        return metaDictSet(requestId, metaDict, "");
    }
    
    @Override
    public MetaDict metaDictSet(String requestId, MetaDict metaDict, String relativePath) {
        final Timer.Context t = metaDictSetTimer.time();
        try {
            Preconditions.checkNotNull(metaDict);
            
            if(Strings.isNullOrEmpty(relativePath)) {
                configManager.createOrUpdateConfig(metaDict);
            } else {
                configManager.createOrUpdateConfig(metaDict, relativePath);
            }
            log.info("metaDictSet [{},{}] -> ok", requestId, metaDict.getName());
            return metaDict;
        } catch (Exception e) {
            log.error("metaDictSet [{},{}] -> error", requestId, metaDict.getName(), e);
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
