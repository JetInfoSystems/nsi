package jet.isur.nsi.services;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.NsiGenericService;
import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaParamValue;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.sql.SqlDao;
import jet.scdp.metrics.api.Metrics;
import jet.scdp.metrics.api.MetricsDomain;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;

@MetricsDomain(name = "genericNsiService")
public class NsiGenericServiceImpl implements NsiGenericService {

    private static final Logger log = LoggerFactory
            .getLogger(NsiGenericServiceImpl.class);
    private final Timer dictCountTimer;
    private final Timer dictListTimer;
    private final Timer dictGetTimer;
    private final Timer dictSaveTimer;
    private final Timer dictBatchSaveTimer;
    private final Timer dictDeleteTimer;

    public NsiGenericServiceImpl(Metrics metrics) {
        dictCountTimer = metrics.timer(getClass(), "dictCount");
        dictListTimer = metrics.timer(getClass(), "dictList");
        dictGetTimer = metrics.timer(getClass(), "dictGet");
        dictSaveTimer = metrics.timer(getClass(), "dictSave");
        dictBatchSaveTimer = metrics.timer(getClass(), "dictBatchSave");
        dictDeleteTimer = metrics.timer(getClass(), "dictDelete");
    }

    private NsiConfigManager configManager;
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter, SqlDao sqlDao) {
        return dictCount(requestId, query, filter, sqlDao, null, null);
    }

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictCountTimer.time();
        try {
            long count;
            checkSourceQuery(query, sourceQueryName);

            try (Connection connection = dataSource.getConnection()) {
                count = sqlDao.count(connection, query, filter, sourceQueryName, sourceQueryParams);
            }
            log.info("dictCount [{},{}] -> ok [{}]", requestId, query.getDict()
                    .getName(), count);
            return count;
        } catch (Exception e) {
            log.error("dictCount [{},{}] -> error", requestId, query.getDict()
                    .getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size, SqlDao sqlDao) {
        return dictList(requestId, query, filter, sortList, offset, size, sqlDao, null, null);
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictListTimer.time();
        try {
            List<DictRow> data;
            checkSourceQuery(query, sourceQueryName);

            try (Connection connection = dataSource.getConnection()) {
                data = sqlDao.list(connection, query, filter, sortList, offset,
                        size, sourceQueryName, sourceQueryParams);
            }
            log.info("dictList [{},{}] -> ok [{}]", requestId, query.getDict()
                    .getName(), data.size());
            return data;
        } catch (Exception e) {
            log.error("dictList [{},{}] -> error", requestId, query.getDict()
                    .getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    private void checkSourceQuery(NsiQuery query, String sourceQueryName) {
        NsiConfigDict dict = query.getDict();
        if(sourceQueryName != null) {
            Preconditions.checkNotNull(dict.getSourceQuery(sourceQueryName),"dict %s source query %s not exists",dict.getName(), sourceQueryName);
        } else {
            Preconditions.checkNotNull(dict.getTable(),"dict %s has not table, source query must set",dict.getName());
        }
    }

    @Override
    public DictRow dictGet(String requestId, NsiConfigDict dict, DictRowAttr id, SqlDao sqlDao) {
        final Timer.Context t = dictGetTimer.time();
        try {
            NsiQuery query = new NsiQuery(configManager.getConfig(), dict);
            query.addAttrs();
            DictRow data;
            try (Connection connection = dataSource.getConnection()) {
                data = sqlDao.get(connection, query, id);
            }
            log.info("dictGet [{},{}] -> ok", requestId, dict.getName());
            return data;
        } catch (Exception e) {
            log.error("dictGet [{},{}] -> error", requestId, dict.getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictSave(String requestId, NsiConfigDict dict, DictRow data, SqlDao sqlDao) {
        final Timer.Context t = dictSaveTimer.time();
        try {
            NsiQuery query = new NsiQuery(configManager.getConfig(), dict);
            query.addAttrs();
            DictRowBuilder builder = new DictRowBuilder(query, data);
            DictRow outData;

            boolean isInsert = false;
            if (builder.getIdAttr() == null
                    || builder.getIdAttr().getValues() == null
                    || builder.getIdAttr().getValues().size() == 0
                    || builder.getIdAttr().getValues().get(0) == null) {
                isInsert = true;
            }
            try (Connection connection = dataSource.getConnection()) {
                if(dict.getLastChangeAttr() != null ) {
                    builder.lastChangeAttr(DateTime.now(DateTimeZone.UTC));
                }
                if (isInsert) {
                    builder.idAttrNull();
                    if(dict.getDeleteMarkAttr() != null) {
                        // если явно не задан то false
                        if(builder.getDeleteMarkAttr() == null) {
                            builder.deleteMarkAttr(false);
                        }
                    }
                    outData = sqlDao.save(connection, query, data, isInsert);
                } else {
                    outData = sqlDao.save(connection, query, data, isInsert);
                }
            }
            if (isInsert) {
                log.info("dictSave [{},{}] -> inserted [{}]", requestId,
                        dict.getName(), builder.getIdAttr());
            } else {
                log.info("dictSave [{},{}] -> updated [{}]", requestId,
                        dict.getName(), builder.getIdAttr());
            }
            return outData;
        } catch (Exception e) {
            log.error("dictSave [{},{}] -> error", requestId, dict.getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictDelete(String requestId, NsiConfigDict dict,
            DictRowAttr id, Boolean value, SqlDao sqlDao) {
        final Timer.Context t = dictDeleteTimer.time();
        try {
            NsiQuery query = new NsiQuery(configManager.getConfig(), dict);
            query.addAttrs();
            DictRow outData;
            try (Connection connection = dataSource.getConnection()) {
                DictRow data = sqlDao.get(connection, query, id);
                DictRowBuilder builder = new DictRowBuilder(query, data);
                builder.deleteMarkAttr(value);
                outData = sqlDao.update(connection, query, builder.build());
            }
            log.info("dictDelete [{},{},{},{}] -> ok", requestId,
                    dict.getName(), id, value);
            return outData;
        } catch (Exception e) {
            log.error("dictDelete [{},{},{},{}] -> error", requestId,
                    dict.getName(), id, value, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictBatchSave(String requestId, NsiConfigDict dict,
            List<DictRow> dataList, SqlDao sqlDao) {
        final Timer.Context t = dictBatchSaveTimer.time();
        try {
            NsiQuery query = new NsiQuery(configManager.getConfig(), dict);
            query.addAttrs();
            List<DictRow> result = new ArrayList<>(dataList.size());

            for (DictRow data : dataList) {
                DictRowBuilder builder = new DictRowBuilder(query, data);
                DictRow outData;
                boolean isInsert = false;
                if (builder.getIdAttr() == null
                        || builder.getIdAttr().getValues() == null
                        || builder.getIdAttr().getValues().size() == 0
                        || builder.getIdAttr().getValues().get(0) == null) {
                    isInsert = true;
                }

                try (Connection connection = dataSource.getConnection()) {
                    if (isInsert) {
                        builder.idAttrNull();
                        outData = sqlDao.save(connection, query, data, isInsert);
                    } else {
                        outData = sqlDao.save(connection, query, data, isInsert);
                    }
                }
                if (isInsert) {
                    log.info("dictBatchSave [{},{}] -> inserted [{}]",
                            requestId, dict.getName(), builder.getIdAttr());
                } else {
                    log.info("dictBatchSave [{},{}] -> updated [{}]",
                            requestId, dict.getName(), builder.getIdAttr());
                }
                result.add(outData);
            }
            return result;
        } catch (Exception e) {
            log.error("dictBatchSave [{},{},{}] -> error", requestId,
                    dict.getName(), dataList.size(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    public void setConfigManager(NsiConfigManager configManager) {
        this.configManager = configManager;
    }

}
