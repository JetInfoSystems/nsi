package jet.isur.nsi.testkit.test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.common.data.DictDependencyGraph;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class BaseTest extends BaseSqlTest {

    protected NsiConfig config;
    protected DefaultSqlDao sqlDao;
    protected DefaultSqlGen sqlGen;
    protected Map<NsiConfigDict,List<DictRow>> testDictRowMap = new HashMap<>();
    public static DateTimeFormatter BASE_DATE_FORMATTER = DateTimeFormat.forPattern("DD.MM.YYYY");

    public void setup() throws Exception {
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        configParams.setLastUserDict("USER_PROFILE");
        config = new NsiConfigManagerFactoryImpl().create(new File(getProperty("database.metadata.path", "/opt/isur/database/metadata")), configParams ).getConfig();
        sqlGen = new DefaultSqlGen();
        sqlDao = new DefaultSqlDao();
        sqlDao.setSqlGen(sqlGen);

    }

    @Override
    public void cleanup() {
        cleanTestDictRows();
        super.cleanup();
    }

    private void cleanTestDictRows() {
        try(Connection c = dataSource.getConnection()) {
            DictDependencyGraph g = DictDependencyGraph.build(config, testDictRowMap.keySet());
            List<NsiConfigDict> testDictList = g.sort();
            Collections.reverse(testDictList);

            for (NsiConfigDict dict : testDictList) {
                DictRowBuilder builder = builder(dict);
                // удаляем данные
                try(PreparedStatement ps = c.prepareStatement("delete from " + dict.getTable() + " where " + dict.getIdAttr().getName() + "=?")) {
                    if(testDictRowMap.containsKey(dict)) {
                        for ( DictRow data : testDictRowMap.get(dict)) {
                            builder.setPrototype(data);
                            ps.setLong(1, builder.getLongIdAttr());
                            ps.execute();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void addTestDictRow(NsiQuery query, DictRow data) {
        NsiConfigDict dict = query.getDict();
        if(!testDictRowMap.containsKey(dict)) {
            testDictRowMap.put(dict,new ArrayList<DictRow>());
        }
        testDictRowMap.get(dict).add(data);
    }

    protected DictRowBuilder defaultBuilder(String dictName) {
        return defaultBuilder(query(dictName));
    }

    protected DictRowBuilder defaultBuilder(NsiQuery query) {
        DictRowBuilder result = builder(query);
        for ( NsiQueryAttr attr : query.getAttrs()) {
            result.attr(attr.getAttr().getName(), (String)null);
        }
        return result.deleteMarkAttr(false);
    }

    protected DictRowBuilder builder(String dictName, DictRow data) {
        DictRowBuilder builder = builder(dictName);
        builder.setPrototype(data);
        return builder;
    }

    protected DictRowBuilder builder(String dictName) {
        return builder(config.getDict(dictName));
    }

    protected DictRowBuilder builder(NsiConfigDict dict) {
        return builder(query(dict));
    }

    protected DictRowBuilder builder(NsiQuery query) {
        return new DictRowBuilder(query);
    }

    protected NsiQuery query(String dictName) {
        return query(config.getDict(dictName));
    }

    protected NsiQuery query(NsiConfigDict dict) {
        return new NsiQuery(config, dict).addAttrs();
    }
}
