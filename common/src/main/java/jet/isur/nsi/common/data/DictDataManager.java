package jet.isur.nsi.common.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.sql.SqlDao;

import com.google.common.base.Joiner;
import jet.isur.nsi.api.tx.NsiSession;

public class DictDataManager {

    private NsiConfig config;
    private DataSource dataSource;
    private SqlDao sqlDao;

    public DictRow get(String dictName, DictRowAttr id) throws SQLException {
        NsiConfigDict dict = getDict(dictName);

        try(NsiSession session = new NsiSession(dataSource)) {
            NsiQuery query = dict.query().addAttrs();
            return sqlDao.get(session.getConnection(), query, id);
        }
    }

    protected NsiConfigDict getDict(String dictName) {
        NsiConfigDict dict = config.getDict(dictName);
        if(dict == null) {
            throw new NsiDataException(Joiner.on(" ").join("Dict dont exists:",dictName));
        }
        return dict;
    }

    public DictRow save(String dictName, DictRow data, boolean insert) throws SQLException {
        NsiConfigDict dict = config.getDict(dictName);
        try(NsiSession session = new NsiSession(dataSource)) {
            NsiQuery query = dict.query().addAttrs();
            return sqlDao.save(session.getConnection(), query, data, insert);
        }
    }

    public List<DictRow> list(String dictName, BoolExp filter, List<SortExp> sortList, int offset, int size) throws SQLException {
        NsiConfigDict dict = getDict(dictName);

        try(NsiSession session = new NsiSession(dataSource)) {
            NsiQuery query = dict.query().addTableObjectAttrs();
            return sqlDao.list(session.getConnection(), query, filter, sortList, offset, size);
        }
    }

    public long count(String dictName, BoolExp filter) throws SQLException {
        NsiConfigDict dict = getDict(dictName);
        try(NsiSession session = new NsiSession(dataSource)) {
            NsiQuery query = dict.query().addTableObjectAttrs();
            return sqlDao.count(session.getConnection(), query, filter);
        }
    }

    public void setConfig(NsiConfig config) {
        this.config = config;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setSqlDao(SqlDao sqlDao) {
        this.sqlDao = sqlDao;
    }


}
