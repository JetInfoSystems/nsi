package jet.isur.nsi.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowAttrBuilder;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;

public class DBAppender {

    Logger log = LoggerFactory.getLogger(DBAppender.class);

    private DataSource dataSource;
    private NsiConfig config;
    private DefaultSqlGen defaultSqlGen;
    private DefaultSqlDao defaultSqlDao;

    public DBAppender(DataSource dataSource, NsiConfig config) {
        this.dataSource = dataSource;
        this.config = config;
        this.defaultSqlGen = new DefaultSqlGen();
        this.defaultSqlDao = new DefaultSqlDao();
    }

    /**
     * Добавление строк в таблицу справочника
     * @param dict описание справочника
     * @param dataList данные для вставки в базу
     * @return данные с заполненными id
     */
    public  List<DictRow> addData(String dictName, List<DictRow> dataList){
        log.info("DBAppender addData "+dictName+ " rows count="+dataList.size());
        NsiConfigDict configDict = config.getDict(dictName);
        NsiQuery query = new NsiQuery(config, configDict).addAttrs();

        try (Connection connection = dataSource.getConnection()) {
            String sql = defaultSqlGen.getRowInsertSql(query, false);
            try (PreparedStatement psGetId = connection
                    .prepareStatement("select " + configDict.getSeq()
                            + ".nextval from dual");
                    PreparedStatement ps = connection
                            .prepareStatement(sql)) {
                DictRowBuilder builder = null;
                for (DictRow data : dataList) {
                    ResultSet rs = psGetId.executeQuery();
                    rs.next();
                    long id = rs.getLong(1);
                    // проблема с иерархией в справочнике PARAM
                    if (data.getAttrs().get("PARENT_ID") != null) {
                        data.getAttrs().put("PARENT_ID", DictRowAttrBuilder.from(id));
                    }
                    builder = new DictRowBuilder(query, data).idAttr(id);
                    defaultSqlDao.setParamsForInsert(query, builder.build(), ps);
                    ps.addBatch();
                }
                ps.executeBatch();
                return dataList;
            }
        } catch (SQLException e) {
            log.error("Ошибка добавления данных в "+dictName, e);
        }
        return null;
    }



    public boolean cleanData(String dictName){

        log.info("appender cleanData "+dictName);

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement ps = connection
                    .prepareStatement("delete from " + dictName
                            + " where ID > 0 ");) {
                ps.executeQuery();
            }
        } catch (SQLException e) {
            log.error("Ошибка удаления данных в "+dictName, e);
            return false;
        }
        return true;
    }

}
