package jet.isur.nsi.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBAppender {

    private static final Logger log = LoggerFactory.getLogger(DBAppender.class);

    private final DataSource dataSource;
    private final NsiConfig config;
    private final DefaultSqlGen sqlGen;
    private final DefaultSqlDao sqlDao;

    public DBAppender(DataSource dataSource, NsiConfig config) {
        this.dataSource = dataSource;
        this.config = config;
        this.sqlGen = new DefaultSqlGen();
        this.sqlDao = new DefaultSqlDao();
        this.sqlDao.setSqlGen(sqlGen);
    }

    public List<DictRow> getData(NsiConfigDict dict) {
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        try (Connection connection = dataSource.getConnection()) {
            return sqlDao.list(connection, query, null, null, -1, -1);
        } catch (SQLException e) {
            log.error("Ошибка добавления данных в "+dict.getName(), e);
        }
        return null;

    }

    /**
     * Добавление строк в таблицу справочника
     * @param dict описание справочника
     * @param dataList данные для вставки в базу
     * @return данные с заполненными id
     */
    public  List<DictRow> addData(NsiConfigDict dict, List<DictRow> dataList){
        log.info("DBAppender addData "+dict.getName()+ " rows count="+dataList.size());
        NsiQuery query = new NsiQuery(config, dict).addAttrs();

        try (Connection connection = dataSource.getConnection()) {
            String sql = sqlGen.getRowInsertSql(query, false);
            try (PreparedStatement psGetId = connection
                    .prepareStatement("select " + dict.getSeq()
                            + ".nextval from dual");
                    PreparedStatement ps = connection
                            .prepareStatement(sql)) {
                DictRowBuilder builder = null;
                for (DictRow data : dataList) {
                    ResultSet rs = psGetId.executeQuery();
                    rs.next();
                    long id = rs.getLong(1);
                    // проблема с иерархией в справочнике PARAM
                    /*
                    if (data.getAttrs().get("PARENT_ID") != null) {
                        data.getAttrs().put("PARENT_ID", DictRowAttrBuilder.from(id));
                    }
                    */
                    builder = new DictRowBuilder(query, data).idAttr(id);
                    sqlDao.setParamsForInsert(query, builder.build(), ps);
                    ps.addBatch();
                }
                ps.executeBatch();
                return dataList;
            }
        } catch (SQLException e) {
            log.error("Ошибка добавления данных в "+dict.getName(), e);
        }
        return null;
    }



    public boolean cleanData(NsiConfigDict dict){

        log.info("appender cleanData "+dict.getName());

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("TRUNCATE TABLE " + dict.getTable())) {
                ps.executeQuery();
            }
        } catch (SQLException e) {
            log.error("Ошибка удаления данных в "+dict.getName(), e);
            return false;
        }
        return true;
    }

}
