package jet.isur.nsi.common.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.sql.SqlDao;

import com.google.common.base.Joiner;

public class DictDataManager {

    private NsiConfig config;
    private DataSource dataSource;
    private SqlDao sqlDao;

    public DictRow get(String dictName, DictRowAttr id) throws SQLException {
        NsiConfigDict dict = getDict(dictName);

        try(Connection connection = dataSource.getConnection()) {
            NsiQuery query = new NsiQuery(config, dict).addAttrs();
            return sqlDao.get(connection, query, id);
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
        try(Connection connection = dataSource.getConnection()) {
            NsiQuery query = new NsiQuery(config, dict).addAttrs();
            if(insert) {
                return sqlDao.insert(connection, query, data);
            } else {
                return sqlDao.update(connection, query, data);
            }
        }
    }

    public List<DictRow> list(String dictName, BoolExp filter, List<SortExp> sortList, int offset, int size) throws SQLException {
        NsiConfigDict dict = getDict(dictName);

        try(Connection connection = dataSource.getConnection()) {
            NsiQuery query = new NsiQuery(config, dict).addTableObjectAttrs();
            return sqlDao.list(connection, query, filter, sortList, offset, size);
        }
    }

    public long count(String dictName, BoolExp filter) throws SQLException {
        NsiConfigDict dict = getDict(dictName);
        try(Connection connection = dataSource.getConnection()) {
            NsiQuery query = new NsiQuery(config, dict).addTableObjectAttrs();
            return sqlDao.count(connection, query, filter);
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
