package jet.isur.nsi.testkit.test;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.DictRowBuilder;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.common.data.DictDependencyGraph;
import jet.isur.nsi.testkit.utils.DaoUtils;

import org.junit.After;
import org.junit.Before;

public class BaseSqlTest {
    protected DataSource dataSource;
    protected Properties properties;
    protected Map<NsiConfigDict, List<DictRow>> testDictRowMap = new HashMap<>();
    protected NsiConfig config;

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
        try (Connection c = dataSource.getConnection()) {
            if(testDictRowMap.size() > 0) {
                DictDependencyGraph g = DictDependencyGraph.build(config,
                        testDictRowMap.keySet());
                List<NsiConfigDict> testDictList = g.sort();
                Collections.reverse(testDictList);

                for (NsiConfigDict dict : testDictList) {
                    if (testDictRowMap.containsKey(dict)) {
                        for (DictRow data : testDictRowMap.get(dict)) {
                            // попробуем удалить дочерние (неучтенные данные -
                            // их могли добавить во время выполнения save, например
                            for (NsiConfigDict childDict : testDictList) {
                                for (NsiConfigAttr ref : childDict.getAttrs()) {
                                    if (MetaAttrType.REF.equals(ref.getType())) {
                                        if (ref.getRefDictName().equals(dict.getTable())) {
                                            try (PreparedStatement ps1 = c.prepareStatement("delete from "
                                                    + childDict.getTable() + " where " + ref.getName() + "=?")) {
                                                ps1.setLong(1, data.getIdAttrLong());
                                                ps1.execute();
                                                }
                                            }
                                        }
                                    }
                                }
                            // удаляем данные
                            try (PreparedStatement ps = c.prepareStatement("delete from "
                                    + dict.getTable() + " where "
                                    + dict.getIdAttr().getName() + "=?")) {
                                        ps.setLong(1, data.getIdAttrLong());
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
