package jet.isur.nsi.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;

import jet.isur.nsi.api.NsiGenericService;
import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.BoolExpBuilder;
import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.DictRowBuilder;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaParamValue;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.sql.SqlDao;
import jet.isur.nsi.api.tx.NsiTransaction;
import jet.isur.nsi.api.tx.NsiTransactionCallback;
import jet.isur.nsi.api.tx.NsiTransactionService;
import jet.isur.nsi.api.tx.NsiTransactionTemplate;
import jet.scdp.metrics.api.Metrics;
import jet.scdp.metrics.api.MetricsDomain;

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
    private final Timer dictMergeByExternalId;
    
    public NsiGenericServiceImpl(Metrics metrics) {
        dictCountTimer = metrics.timer(getClass(), "dictCount");
        dictListTimer = metrics.timer(getClass(), "dictList");
        dictGetTimer = metrics.timer(getClass(), "dictGet");
        dictSaveTimer = metrics.timer(getClass(), "dictSave");
        dictBatchSaveTimer = metrics.timer(getClass(), "dictBatchSave");
        dictDeleteTimer = metrics.timer(getClass(), "dictDelete");
        dictMergeByExternalId = metrics.timer(getClass(), "dictMergeByExternalId");
    }

    private NsiTransactionService transactionService;

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter, SqlDao sqlDao) {
        return dictCount(requestId, query, filter, sqlDao, null, null);
    }

    @Override
    public long dictCount(NsiTransaction tx, NsiQuery query, BoolExp filter, SqlDao sqlDao) {
        return dictCount(tx, query, filter, sqlDao, null, null);
    }

    private long dictCountInternal(NsiTransaction tx, NsiQuery query, BoolExp filter,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        checkSourceQuery(query, sourceQueryName);
        return sqlDao.count(tx.getConnection(), query, filter, sourceQueryName, sourceQueryParams);
    }

    @Override
    public long dictCount(NsiTransaction tx, NsiQuery query, BoolExp filter,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictCountTimer.time();
        try {
            long count;
            count = dictCountInternal(tx, query, filter, sqlDao, sourceQueryName, sourceQueryParams);
            log.info("dictCount [{},{}] -> ok [{}]", tx.getRequestId(), query.getDict().getName(), count);
            return count;
        } catch (Exception e) {
            log.error("dictCount [{},{}] -> error", tx.getRequestId(), query.getDict()
                    .getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictCountTimer.time();
        try(NsiTransaction tx = transactionService.createTransaction(requestId)) {
            try {
                return dictCountInternal(tx, query, filter, sqlDao, sourceQueryName, sourceQueryParams);
            } catch (Exception e) {
                log.error("dictCount [{},{}] -> error", requestId, query.getDict().getName(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictCount [{},{}] -> error", requestId, query.getDict().getName(), e);
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
    public List<DictRow> dictList(NsiTransaction tx, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size, SqlDao sqlDao) {
        return dictList(tx, query, filter, sortList, offset, size, sqlDao, null, null);
    }

    private List<DictRow> dictListInternal(NsiTransaction tx, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        checkSourceQuery(query, sourceQueryName);

        return sqlDao.list(tx.getConnection(), query, filter, sortList, offset,
                    size, sourceQueryName, sourceQueryParams);
    }

    @Override
    public List<DictRow> dictList(NsiTransaction tx, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictListTimer.time();
        try {
            List<DictRow> data;
            data = dictListInternal(tx, query, filter, sortList, offset,
                        size, sqlDao, sourceQueryName, sourceQueryParams);
            log.info("dictList [{},{}] -> ok [{}]", tx.getRequestId(), query.getDict().getName(), data.size());
            return data;
        } catch (Exception e) {
            log.error("dictList [{},{}] -> error", tx.getRequestId(), query.getDict().getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            SqlDao sqlDao, String sourceQueryName,
            Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictListTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId)) {
            try {
                List<DictRow> data = dictListInternal(tx, query, filter, sortList, offset,
                        size, sqlDao, sourceQueryName, sourceQueryParams);
                log.info("dictList [{},{}] -> ok [{}]", requestId, query.getDict().getName(), data.size());
                return data;
            } catch (Exception e) {
                log.error("dictList [{},{}] -> error", requestId, query.getDict().getName(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictList [{},{}] -> error", requestId, query.getDict().getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    private void checkSourceQuery(NsiQuery query, String sourceQueryName) {
        NsiConfigDict dict = query.getDict();
        if(sourceQueryName != null) {
            Preconditions.checkNotNull(dict.getSourceQuery(sourceQueryName),"dict %s source query %s not exists",dict.getName(), sourceQueryName);
        } else if(dict.getTable() == null) {
            Preconditions.checkNotNull(dict.getSourceQuery(NsiQuery.MAIN_QUERY),"dict %s has not table or default query, source query must set",dict.getName());
        }
    }

    private DictRow dictGetInternal(NsiTransaction tx, NsiConfigDict dict, DictRowAttr id, SqlDao sqlDao) {
        NsiQuery query = dict.query().addAttrs();
        return sqlDao.get(tx.getConnection(), query, id);
    }

    @Override
    public DictRow dictGet(NsiTransaction tx, NsiConfigDict dict, DictRowAttr id, SqlDao sqlDao) {
        final Timer.Context t = dictGetTimer.time();
        try {
            DictRow data = dictGetInternal(tx, dict, id, sqlDao);
            log.info("dictGet [{},{}] -> ok", tx.getRequestId(), dict.getName());
            return data;
        } catch (Exception e) {
            log.error("dictGet [{},{}] -> error", tx.getRequestId(), dict.getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictGet(String requestId, NsiConfigDict dict, DictRowAttr id, SqlDao sqlDao) {
        final Timer.Context t = dictGetTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId)) {
            try {
                DictRow data = dictGetInternal(tx, dict, id, sqlDao);
                log.info("dictGet [{},{}] -> ok", requestId, dict.getName());
                return data;
            } catch (Exception e) {
                log.error("dictGet [{},{}] -> error", requestId, dict.getName(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictGet [{},{}] -> error", requestId, dict.getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    private DictRow dictSaveInternal(NsiTransaction tx, DictRow data, SqlDao sqlDao) {
        NsiConfigDict dict = data.getDict();
        NsiQuery query = dict.query().addAttrs(data);
        DictRow outData;

        boolean isInsert = data.isIdAttrEmpty();
        outData = sqlDao.save(tx.getConnection(), query, data, isInsert);
        if (isInsert) {
        	query.addStdAttrs();
            log.info("dictSave [{},{}] -> inserted [{}]", tx.getRequestId(),
                    dict.getName(), data.getIdAttr());
        } else {
            log.info("dictSave [{},{}] -> updated [{}]", tx.getRequestId(),
                    dict.getName(), data.getIdAttr());
        }
        return outData;
    }

    @Override
    public DictRow dictSave(NsiTransaction tx, DictRow data, SqlDao sqlDao) {
        final Timer.Context t = dictSaveTimer.time();
        try {
            return dictSaveInternal(tx, data, sqlDao);
        } catch (Exception e) {
            log.error("dictSave [{},{}] -> error", tx.getRequestId(), data.getDict().getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictSave(String requestId, DictRow data, SqlDao sqlDao) {
        final Timer.Context t = dictSaveTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId)) {
            try {
                return dictSaveInternal(tx, data, sqlDao);
            } catch (Exception e) {
                log.error("dictSave [{},{}] -> error", requestId, data.getDict().getName(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictSave [{},{}] -> error", requestId, data.getDict().getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    public DictRow dictDeleteInternal(NsiTransaction tx, NsiConfigDict dict,
            DictRowAttr id, Boolean value, SqlDao sqlDao) {
        NsiQuery query = dict.query().addAttrs();
        DictRow data = sqlDao.get(tx.getConnection(), query, id);
        data.setDeleteMarkAttr(value);
        DictRow outData = sqlDao.update(tx.getConnection(), query, data);
        log.info("dictDelete [{},{},{},{}] -> ok", tx.getRequestId(), dict.getName(), id, value);
        return outData;
    }

    @Override
    public DictRow dictDelete(NsiTransaction tx, NsiConfigDict dict,
            DictRowAttr id, Boolean value, SqlDao sqlDao) {
        final Timer.Context t = dictDeleteTimer.time();
        try {
            return dictDeleteInternal(tx, dict, id, value, sqlDao);
        } catch (Exception e) {
            log.error("dictDelete [{},{},{},{}] -> error", tx.getRequestId(),
                    dict.getName(), id, value, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictDelete(String requestId, NsiConfigDict dict,
            DictRowAttr id, Boolean value, SqlDao sqlDao) {
        final Timer.Context t = dictDeleteTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId)) {
            try {
                return dictDeleteInternal(tx, dict, id, value, sqlDao);
            } catch (Exception e) {
                log.error("dictDelete [{},{},{},{}] -> error", requestId, dict.getName(), id, value, e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictDelete [{},{},{},{}] -> error", requestId, dict.getName(), id, value, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    private List<DictRow> dictBatchSaveInternal(NsiTransaction tx, List<DictRow> dataList, SqlDao sqlDao) {
        List<DictRow> result = new ArrayList<>(dataList.size());

        for (DictRow data : dataList) {
            NsiConfigDict dict = data.getDict();
            NsiQuery query = dict.query().addAttrs();

            DictRowBuilder builder = data.builder();
            DictRow outData;
            boolean isInsert = data.isIdAttrEmpty();

                if (isInsert) {
                    builder.idAttrNull();
                    outData = sqlDao.save(tx.getConnection(), query, data, isInsert);
                } else {
                    outData = sqlDao.save(tx.getConnection(), query, data, isInsert);
                }
            if (isInsert) {
                log.info("dictBatchSave [{},{}] -> inserted [{}]",
                        tx.getRequestId(), dict.getName(), data.getIdAttr());
            } else {
                log.info("dictBatchSave [{},{}] -> updated [{}]",
                        tx.getRequestId(), dict.getName(), data.getIdAttr());
            }
            result.add(outData);
        }
        return result;
    }

    @Override
    public List<DictRow> dictBatchSave(NsiTransaction tx, List<DictRow> dataList, SqlDao sqlDao) {
        final Timer.Context t = dictBatchSaveTimer.time();
        try {
            return dictBatchSaveInternal(tx, dataList, sqlDao);
        } catch (Exception e) {
            log.error("dictBatchSave [{},{}] -> error", tx.getRequestId(), dataList.size(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictBatchSave(String requestId, List<DictRow> dataList, SqlDao sqlDao) {
        final Timer.Context t = dictBatchSaveTimer.time();
        try(NsiTransaction tx = transactionService.createTransaction(requestId)) {
            try {
                return dictBatchSaveInternal(tx, dataList, sqlDao);
            } catch (Exception e) {
                log.error("dictBatchSave [{},{}] -> error", requestId, dataList.size(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictBatchSave [{},{}] -> error", requestId, dataList.size(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    public void setTransactionService(NsiTransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    public DictRowAttr dictMergeByExternalId(NsiTransaction tx, final DictRow data, SqlDao sqlDao) {
        final Timer.Context t = dictMergeByExternalId.time();
        try {
			return dictMergeByExternalIdInternal(tx, data, sqlDao);
        } catch (Exception e) {
        	log.error("dictMergeByExternalId [{}] -> error", tx.getRequestId(), e);
        	throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }
    
	private DictRowAttr dictMergeByExternalIdInternal(NsiTransaction tx, DictRow data, SqlDao sqlDao) {
		return sqlDao.mergeByExternalId(tx.getConnection(), data);
	}
}
