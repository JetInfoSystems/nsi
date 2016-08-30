package jet.nsi.services;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;

import jet.metrics.api.Metrics;
import jet.metrics.api.MetricsDomain;
import jet.nsi.api.NsiGenericService;
import jet.nsi.api.NsiService;
import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.DictRowAttr;
import jet.nsi.api.model.MetaParamValue;
import jet.nsi.api.model.SortExp;
import jet.nsi.api.sql.SqlDao;
import jet.nsi.api.tx.NsiTransaction;

@MetricsDomain(name = "nsiService")
public class NsiServiceImpl implements NsiService {

    private final Timer dictCountTimer;
    private final Timer dictListTimer;
    private final Timer dictGetTimer;
    private final Timer dictSaveTimer;
    private final Timer dictBatchSaveTimer;
    private final Timer dictDeleteTimer;
    private final Timer dictMergeByExternalAttrs;
    
    public NsiServiceImpl(Metrics metrics) {
        dictCountTimer = metrics.timer(getClass(), "dictCount");
        dictListTimer = metrics.timer(getClass(), "dictList");
        dictGetTimer = metrics.timer(getClass(), "dictGet");
        dictSaveTimer = metrics.timer(getClass(), "dictSave");
        dictBatchSaveTimer = metrics.timer(getClass(), "dictBatchSave");
        dictDeleteTimer = metrics.timer(getClass(), "dictDelete");
        dictMergeByExternalAttrs = metrics.timer(getClass(), "dictMergeByExternalAttrs");
    }

    private static final Logger log = LoggerFactory.getLogger(NsiServiceImpl.class);
    
    private SqlDao sqlDao;
    private NsiGenericService nsiGenericService;
    
    public void setSqlDao(SqlDao sqlDao) {
        this.sqlDao = sqlDao;
    }

    
    
    public void setNsiGenericService(NsiGenericService nsiGenericService) {
        this.nsiGenericService = nsiGenericService;
    }
    
    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter) {
        return dictCount(requestId, query, filter, null, null);
    }

    @Override
    public long dictCount(NsiTransaction tx, NsiQuery query, BoolExp filter) {
        return dictCount(tx, query, filter, null, null);
    }

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictCountTimer.time();
        try {
            return nsiGenericService.dictCount(requestId, query, filter, sqlDao, sourceQueryName, sourceQueryParams);
        } finally {
            t.stop();
        }
    }

    @Override
    public long dictCount(NsiTransaction tx, NsiQuery query, BoolExp filter,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictCountTimer.time();
        try {
            return nsiGenericService.dictCount(tx, query, filter, sqlDao, sourceQueryName, sourceQueryParams);
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size) {
        return dictList(requestId, query, filter, sortList, offset, size, null, null);
    }

    @Override
    public List<DictRow> dictList(NsiTransaction tx, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size) {
        return dictList(tx, query, filter, sortList, offset, size, null, null);
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictListTimer.time();
        try {
            return nsiGenericService.dictList(requestId, query, filter, sortList, offset, size, sqlDao, sourceQueryName, sourceQueryParams);
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictList(NsiTransaction tx, NsiQuery query,
            BoolExp filter, List<SortExp> sortList, long offset, int size,
            String sourceQueryName, Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictListTimer.time();
        try {
            return nsiGenericService.dictList(tx, query, filter, sortList, offset, size, sqlDao, sourceQueryName, sourceQueryParams);
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictGet(String requestId, NsiConfigDict dict, DictRowAttr id) {
        final Timer.Context t = dictGetTimer.time();
        try {
            return nsiGenericService.dictGet(requestId, dict, id, sqlDao);
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictGet(NsiTransaction tx, NsiConfigDict dict, DictRowAttr id) {
        final Timer.Context t = dictGetTimer.time();
        try {
            return nsiGenericService.dictGet(tx, dict, id, sqlDao);
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictSave(String requestId, DictRow data) {
        final Timer.Context t = dictSaveTimer.time();
        try {
            return nsiGenericService.dictSave(requestId, data, sqlDao);
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictSave(NsiTransaction tx, DictRow data) {
        final Timer.Context t = dictSaveTimer.time();
        try {
            return nsiGenericService.dictSave(tx, data, sqlDao);
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictDelete(String requestId, NsiConfigDict dict,
            DictRowAttr id, Boolean value) {
        final Timer.Context t = dictDeleteTimer.time();
        try {
            return nsiGenericService.dictDelete(requestId, dict, id, value, sqlDao);
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictDelete(NsiTransaction tx, NsiConfigDict dict,
            DictRowAttr id, Boolean value) {
        final Timer.Context t = dictDeleteTimer.time();
        try {
            return nsiGenericService.dictDelete(tx, dict, id, value, sqlDao);
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictBatchSave(String requestId, List<DictRow> dataList) {
        final Timer.Context t = dictBatchSaveTimer.time();
        try {
            return nsiGenericService.dictBatchSave(requestId, dataList, sqlDao);
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictBatchSave(NsiTransaction tx, List<DictRow> dataList) {
        final Timer.Context t = dictBatchSaveTimer.time();
        try {
            return nsiGenericService.dictBatchSave(tx, dataList, sqlDao);
        } finally {
            t.stop();
        }
    }

    

    
    public DictRow dictMergeByExternalAttrs(String requestId, final DictRow data) {
        final Timer.Context t = dictMergeByExternalAttrs.time();
        try {
        	return nsiGenericService.dictMergeByExternalAttrs(requestId, data, sqlDao);
        } finally {
            t.stop();
        }
    }

    
    public DictRow dictMergeByExternalAttrs(final NsiTransaction tx, final DictRow data) {
        final Timer.Context t = dictMergeByExternalAttrs.time();
        try {
        	return nsiGenericService.dictMergeByExternalAttrs(tx, data, sqlDao);
        } finally {
            t.stop();
        }
    }


    

}
