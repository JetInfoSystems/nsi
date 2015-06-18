package jet.isur.nsi.services.impl;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import jet.isur.nsi.api.NsiService;
import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.common.sql.SqlDao;
import jet.scdp.metrics.api.Metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;

public class NsiServiceImpl implements NsiService {

    private static final Logger log = LoggerFactory.getLogger(NsiServiceImpl.class);
    private final Timer dictCountTimer;
    private final Timer dictListTimer;
    private final Timer dictGetTimer;
    private final Timer dictSaveTimer;
    private final Timer dictDeleteTimer;

    public NsiServiceImpl(Metrics metrics) {
        dictCountTimer = metrics.timer(getClass(), "dictCount");
        dictListTimer = metrics.timer(getClass(), "dictList");
        dictGetTimer = metrics.timer(getClass(), "dictGet");
        dictSaveTimer = metrics.timer(getClass(), "dictSave");
        dictDeleteTimer = metrics.timer(getClass(),"dictDelete");
    }

    private SqlDao sqlDao;
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setSqlDao(SqlDao sqlDao) {
        this.sqlDao = sqlDao;
    }

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter) {
        final Timer.Context t = dictCountTimer.time();
        try {
            long count;
            try(Connection connection = dataSource.getConnection()) {
                count = sqlDao.count(connection, query, filter);
            }
            log.info("dictCount [{},{}] -> ok [{}]",requestId, query.getDict().getName(), count);
            return count;
        } catch(Exception e) {
            log.error("dictCount [{},{}] -> error",requestId, query.getDict().getName(),e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query, BoolExp filter, List<SortExp> sortList, long offset, int size ) {
        final Timer.Context t = dictListTimer.time();
        try {
            List<DictRow> data;
            try(Connection connection = dataSource.getConnection()) {
                data = sqlDao.list(connection, query, filter, sortList, offset, size);
            }
            log.info("dictList [{}] -> ok [{}]",requestId,data.size());
            return data;
        } catch(Exception e) {
            log.error("dictList [{}] -> error",requestId,e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictGet(String requestId, NsiConfigDict dict, DictRowAttr id) {
        final Timer.Context t = dictGetTimer.time();
        try {
            NsiQuery query = new NsiQuery(dict);
            query.addAttrs();
            DictRow data;
            try(Connection connection = dataSource.getConnection()) {
                data = sqlDao.get(connection, query, id);
            }
            log.info("dictGet [{}] -> ok",requestId);
            return data;
        } catch(Exception e) {
            log.error("dictGet [{}] -> error",requestId,e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictSave(String requestId, NsiConfigDict dict, DictRow data) {
        final Timer.Context t = dictSaveTimer.time();
        try {
            NsiQuery query = new NsiQuery(dict);
            query.addAttrs();
            DictRowBuilder builder = new DictRowBuilder(query, data);
            DictRow outData;

            boolean isInsert = builder.getIdAttr() == null;
            try(Connection connection = dataSource.getConnection()) {
                if(isInsert) {
                    builder.idAttrNull();
                    outData = sqlDao.insert(connection, query, data);
                } else {
                    outData = sqlDao.update(connection, query, data);
                }
            }
            if(isInsert) {
                log.info("dictSave [{}] -> inserted [{}]",requestId,builder.getIdAttr());
            } else {
                log.info("dictSave [{}] -> updated [{}]",requestId,builder.getIdAttr());
            }
            return outData;
        } catch(Exception e) {
            log.error("dictSave [{}] -> error",requestId,e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public DictRow dictDelete(String requestId, NsiConfigDict dict, DictRowAttr id, Boolean value) {
        final Timer.Context t = dictDeleteTimer.time();
        try {
            NsiQuery query = new NsiQuery(dict);
            query.addAttrs();
            DictRow outData;
            try(Connection connection = dataSource.getConnection()) {
                DictRow data = sqlDao.get(connection, query, id);
                DictRowBuilder builder = new DictRowBuilder(query, data);
                builder.deleteMarkAttr(value);
                outData = sqlDao.update(connection, query, builder.build());
            }
            log.info("dictDelete [{},{},{}] -> ok",requestId, id, value);
            return outData;
        } catch(Exception e) {
            log.error("dictDelete [{},{},{}] -> error",requestId, id, value, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

}
