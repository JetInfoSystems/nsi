package jet.isur.nsi.testkit.test;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.jooq.DeleteWhereStep;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.DictRowBuilder;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.platform.NsiPlatform;
import jet.isur.nsi.api.platform.PlatformSqlDao;
import jet.isur.nsi.api.platform.PlatformSqlGen;
import jet.isur.nsi.common.data.DictDependencyGraph;
import jet.isur.nsi.common.platform.oracle.OracleNsiPlatform;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;
import jet.isur.nsi.testkit.utils.DaoUtils;

public class BaseSqlTest {

    protected DataSource dataSource;
    protected Properties properties;
    protected Map<NsiConfigDict, List<DictRow>> testDictRowMap = new HashMap<>();
    protected NsiConfig config;
    protected NsiPlatform platform;
    protected PlatformSqlGen platformSqlGen;
    protected PlatformSqlDao platformSqlDao;
    protected DefaultSqlGen sqlGen;
    protected DefaultSqlDao sqlDao;
    protected static Logger log = LoggerFactory.getLogger(BaseSqlTest.class);

    @Before
    public void setupInternal() throws Exception {
        setup();
    }

    public void setup() throws Exception {
        properties = new Properties();
        File file = new File("target/test-classes/project.properties").getAbsoluteFile();
        try(FileReader reader = new FileReader(file)) {
            properties.load(reader);
        }
        dataSource = DaoUtils.createDataSource("isur", properties);
        sqlGen = new DefaultSqlGen();
        platformSqlGen = platform.getPlatformSqlGen();
        sqlGen.setPlatformSqlGen(platformSqlGen);
        sqlDao = new DefaultSqlDao();
        sqlDao.setSqlGen(sqlGen);
        platformSqlDao = platform.getPlatformSqlDao();
        sqlDao.setPlatformSqlDao(platformSqlDao);
    }

    @After
    public void cleanupInternal() {
        cleanup();
    }

    public void cleanup() {
    }

    protected String getProperty(String key,String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    protected void cleanTestDictRows() {
        try (Connection connection = dataSource.getConnection()) {
            if(testDictRowMap.size() > 0) {
                DictDependencyGraph g = DictDependencyGraph.build(config,
                        testDictRowMap.keySet());
                List<NsiConfigDict> testDictList = g.sort();
                // нужно пройтись по всем справочникам, найти подчиненные и
                // если есть "неучтенные" подчиненные записи - их нужно добавить
                // по всем справочникам как родителям
                for (NsiConfigDict parent : testDictList) {
                    // если для справочника будем удалять данные
                    if (null != testDictRowMap.get(parent)) {
                        // среди всех справочников ищем подчиненные
                        // для этого берем каждый справочник
                        for (NsiConfigDict child : config.getDicts()) {
                            // если это таблица
                            if (null != child.getTable()){
                                // и среди его атрибутов
                                for (NsiConfigAttr ref : child.getAttrs()) {
                                    // ищем атрибуты - ссылки на текущего родителя
                                    if (MetaAttrType.REF.equals(ref.getType())
                                            && getMainDict(ref.getRefDict()).getTable().equals(parent.getTable())) {
                                        // теперь для всех сохраненных значений  парента
                                        for (DictRow curParentValue : testDictRowMap.get(parent)) {
                                            // ищем подчиненные записи
                                            if (null != curParentValue.getIdAttr()
                                                    && null != child.getIdAttr()) {
                                                List<DictRow> data = sqlDao.list(
                                                        connection,
                                                        child.query().addAttr(child.getIdAttr().getName()),// нас интересует только ид-шник
                                                        child.filter().key(ref.getName()).eq().value(curParentValue.getIdAttr()).build(),
                                                        null, -1, -1);
                                                // и добавляем их в список на удаление
                                                for (DictRow row : data)
                                                    addTestDictRow(child.query(), row);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // очищаем циклические ссылки в тестовых данных 
                Map<NsiConfigDict, Set<String>> cycleRefs = g.getCycleRefs();
                for(NsiConfigDict dict : testDictList) {
                    if(cycleRefs.containsKey(dict) && testDictRowMap.containsKey(dict)) {
                        UpdateSetFirstStep<Record> updateSetFirstStep = DSL.using(SQLDialect.DEFAULT).update(DSL.table(dict.getTable()));
                        UpdateSetMoreStep<Record> updateSetMoreStep = null;
                        for (String attrName : cycleRefs.get(dict)) {
                            NsiConfigAttr attr = dict.getAttr(attrName);
                            for ( NsiConfigField field : attr.getFields()) {
                                updateSetMoreStep = updateSetFirstStep.set(DSL.field(field.getName()), (String)null);
                            }
                        }
                        
                        NsiConfigAttr idAttr = dict.getIdAttr();
                        for ( NsiConfigField field : idAttr.getFields()) {
                            updateSetMoreStep.where(DSL.field(field.getName()).eq(DSL.val(null)));
                        }
                        
                        try (PreparedStatement ps = connection.prepareStatement(updateSetMoreStep.getSQL())) {
                            for (DictRow data : testDictRowMap.get(dict)) {
                                int i = 1;
                                for (String attrName : cycleRefs.get(dict)) {
                                    NsiConfigAttr attr = dict.getAttr(attrName);
                                    for ( @SuppressWarnings("unused") NsiConfigField field : attr.getFields()) {
                                        ps.setNull(i, Types.VARCHAR);
                                        i++;
                                    }
                                }
                                for (String value : data.getIdAttr().getValues()) {
                                    ps.setString(i, value);
                                    i++;
                                }
                                ps.execute();
                            }
                        }
                    }
                }
                
                Collections.reverse(testDictList);

                for (NsiConfigDict dict : testDictList) {
                    // удаляем данные
                    if (testDictRowMap.containsKey(dict)) {
                        DeleteWhereStep<Record> deleteWhereStep = DSL.using(SQLDialect.DEFAULT).delete(DSL.table(dict.getTable()));
                        for ( NsiConfigField field : dict.getIdAttr().getFields()) {
                            deleteWhereStep.where(DSL.field(field.getName()).eq(DSL.val(null)));
                        }
                        try (PreparedStatement ps = connection.prepareStatement(deleteWhereStep.getSQL())) {
                            for (DictRow data : testDictRowMap.get(dict)) {
                                int i = 1;
                                for ( String value : data.getIdAttr().getValues()) {
                                    ps.setString(i, value);
                                    i++;
                                }
                                ps.execute();
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private NsiConfigDict getMainDict(NsiConfigDict dict) {
        return dict.getMainDict() != null ? dict.getMainDict() : dict;
    }


    protected void addTestDictRow(NsiQuery query, DictRow data) {
        NsiConfigDict dict = query.getDict();
        if (!testDictRowMap.containsKey(dict)) {
            testDictRowMap.put(dict, new ArrayList<DictRow>());
        }
        testDictRowMap.get(dict).add(data);
    }

    protected DictRowBuilder defaultBuilder(String dictName) {
        return defaultBuilder(query(dictName));
    }

    protected DictRowBuilder defaultBuilder(NsiQuery query) {
        DictRowBuilder result = query.getDict().builder();
        for (NsiQueryAttr attr : query.getAttrs()) {
           result.attr(attr.getAttr().getName(), (String) null);
        }
        return result.deleteMarkAttr(false);
    }

    protected NsiConfigDict dict(String dictName) {
        return config.getDict(dictName);
    }

    protected NsiQuery query(String dictName) {
        return query(config.getDict(dictName));
    }

    protected NsiQuery query(NsiConfigDict dict) {
        return dict.query().addAttrs();
    }


}
