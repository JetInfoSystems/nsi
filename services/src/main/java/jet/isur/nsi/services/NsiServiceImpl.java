package jet.isur.nsi.services;

import java.util.Collection;
import java.util.List;

import jet.isur.nsi.api.NsiGenericService;
import jet.isur.nsi.api.NsiService;
import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaParamValue;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.sql.SqlDao;
import jet.isur.nsi.api.tx.NsiTransaction;
import jet.scdp.metrics.api.Metrics;
import jet.scdp.metrics.api.MetricsDomain;

import com.codahale.metrics.Timer;

@MetricsDomain(name = "nsiService")
public class NsiServiceImpl implements NsiService {

    private final Timer dictCountTimer;
    private final Timer dictListTimer;
    private final Timer dictGetTimer;
    private final Timer dictSaveTimer;
    private final Timer dictBatchSaveTimer;
    private final Timer dictDeleteTimer;

    public NsiServiceImpl(Metrics metrics) {
        dictCountTimer = metrics.timer(getClass(), "dictCount");
        dictListTimer = metrics.timer(getClass(), "dictList");
        dictGetTimer = metrics.timer(getClass(), "dictGet");
        dictSaveTimer = metrics.timer(getClass(), "dictSave");
        dictBatchSaveTimer = metrics.timer(getClass(), "dictBatchSave");
        dictDeleteTimer = metrics.timer(getClass(), "dictDelete");
    }

    private SqlDao sqlDao;
    private NsiGenericService nsiGenericService;

    public void setSqlDao(SqlDao sqlDao) {
        this.sqlDao = sqlDao;
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

    public void setNsiGenericService(NsiGenericService nsiGenericService) {
        this.nsiGenericService = nsiGenericService;
    }

}
