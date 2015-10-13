package jet.isur.nsi.services.sql.test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.DictRowBuilder;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import jet.isur.nsi.testkit.utils.DaoUtils;
import jet.isur.nsi.testkit.utils.DataUtils;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class SqlDaoTest extends BaseSqlTest {

    private DefaultSqlDao sqlDao;
    private DefaultSqlGen sqlGen;
    private NsiConfig config;


    @Override
    public void setup() throws Exception {
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("src/test/resources/metadata1"), configParams ).getConfig();
        sqlGen = new DefaultSqlGen();
        sqlDao = new DefaultSqlDao();
        sqlDao.setSqlGen(sqlGen);
    }

    @Test
    public void testInsertAndGet() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    DictRow outData = insertDict1Row(connection, query, "f1-value");
                    Assert.assertEquals(1L, (long) outData.getIdAttrLong());

                    DictRow getData = sqlDao.get(connection, query, outData.getIdAttr());
                    DataUtils.assertEquals(query, outData, getData);

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testInsertAndGetWithRefFields() throws Exception {
        NsiConfigDict dict1 = config.getDict("dict1");
        NsiConfigDict dict2 = config.getDict("dict2");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict2, connection);
            DaoUtils.recreateTable(dict1, connection);
            try {
                DaoUtils.recreateSeq(dict2, connection);
                DaoUtils.recreateSeq(dict1, connection);
                try {
                    NsiQuery query1 = dict1.query().addAttrs();
                    DictRow dict1Data = insertDict1Row(connection, query1, "f1-value");

                    NsiQuery query2 = dict2.query().addAttrs();
                    long dict1Id = dict1Data.getIdAttrLong();
                    DictRow dict2Data = saveDict2Row(connection, query2, dict1Id, true);

                    NsiQuery query = dict2.query().addAttrs();

                    DictRow getData = sqlDao.get(connection, query, dict2Data.getIdAttr());
                    DataUtils.assertEquals(query, dict2Data, getData);


                } finally {
                    DaoUtils.dropSeq(dict2, connection);
                    DaoUtils.dropSeq(dict1, connection);
                }

            } finally {
                DaoUtils.dropTable(dict2, connection);
                DaoUtils.dropTable(dict1, connection);
            }
        }
    }

    @Test
    public void testInsertAndUpdate() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    DictRow outData = insertDict1Row(connection, query, "f1-value");

                    DictRow inUpdatedData = dict.builder(
                            DictRowBuilder.cloneRow(outData)).attr("f1","f1-value-changed").build();

                    DictRow controlData = DictRowBuilder.cloneRow(inUpdatedData);

                    DictRow outUpdatedData = sqlDao.update(connection, query, inUpdatedData);
                    DataUtils.assertEquals(query, controlData, outUpdatedData);

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testList() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    for (int i = 0; i < 10; i++) {
                        insertDict1Row(connection, query, "value" + i);
                    }

                    List<DictRow> rows = sqlDao.list(connection, query, null, null, -1, -1);

                    Assert.assertEquals(10, rows.size());

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testListReverseSort() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    for (int i = 0; i < 10; i++) {
                        insertDict1Row(connection, query, "value" + i);
                    }


                    List<DictRow> rows = sqlDao.list(connection, query,
                            null,
                            dict.sort().add("f1", false).build(), -1, -1);

                    Assert.assertEquals(10, rows.size());
                    Assert.assertEquals("value9", rows.get(0).getString("f1"));

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testListFilterSort() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    for (int i = 0; i < 10; i++) {
                        insertDict1Row(connection, query, "value" + i);
                    }


                    List<DictRow> rows = sqlDao.list(connection, query,
                            dict.filter()
                                .key("f1").eq().value("value5")
                                .build(),
                            dict.sort().add("f1", false).build(), -1, -1);

                    Assert.assertEquals(1, rows.size());
                    Assert.assertEquals("value5", rows.get(0).getString("f1"));

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testCountFilter() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    for (int i = 0; i < 10; i++) {
                        insertDict1Row(connection, query, "value" + i);
                    }


                    long count = sqlDao.count(connection, query,
                            dict.filter().key("f1").eq().value("value5").build());

                    Assert.assertEquals(1L, count);

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    private DictRow insertDict1Row(Connection connection, NsiQuery query,
            String f1Value) {
        DictRow inData = query.getDict().builder()
                .deleteMarkAttr(false)
                .idAttrNull()
                .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                .lastUserAttr(null)
                .attr("f1", f1Value)
                .attr("ORG_ID", 1L)
                .attr("ORG_ROLE_ID", 2L)
                .build();

        return sqlDao.insert(connection, query, inData);
    }

    private DictRow saveDict2Row(Connection connection, NsiQuery query,
            long  dic1Id, boolean insert) {
        DictRow inData = query.getDict().builder()
                .deleteMarkAttr(false)
                .idAttrNull()
                .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                .lastUserAttr(null)
                .attr("f1", "test")
                .attr("dict1_id", dic1Id)
                .build();

        return sqlDao.save(connection, query, inData, insert);
    }

    @Test
    public void testInsertAndGetChar1Char2() throws Exception {
        NsiConfigDict dict = config.getDict("dict4");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    DictRow inData = dict.builder()
                        .deleteMarkAttr(false)
                        .idAttrNull()
                        .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                        .lastUserAttr(null)
                        .attr("f1", true)
                        .attr("f2", "A")
                        .build();

                    DictRow outData = sqlDao.insert(connection, query, inData);
                    Assert.assertEquals(1L, (long)outData.getIdAttrLong());
                    Assert.assertEquals(true, outData.getBoolean("f1"));
                    Assert.assertEquals("A", outData.getString("f2"));

                    DictRow getData = sqlDao.get(connection, query, outData.getIdAttr());
                    DataUtils.assertEquals(query, outData, getData);

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }


    @Test
    public void testBatchInsert() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        DefaultSqlDao dao = new DefaultSqlDao();
        DefaultSqlGen gen = new DefaultSqlGen();
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {

                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    // ID будем задавать явно, поэтому последовательность не
                    // используем
                    String sql = gen.getRowInsertSql(query, false);
                    try (PreparedStatement psGetId = connection
                            .prepareStatement("select " + dict.getSeq()
                                    + ".nextval from dual");
                            PreparedStatement ps = connection
                                    .prepareStatement(sql)) {

                        DictRowBuilder builder = dict.builder();
                        List<DictRow> dataList = new ArrayList<>();
                        for (Integer i = 0; i < 10; i++) {
                            ResultSet rs = psGetId.executeQuery();
                            rs.next();
                            long id = rs.getLong(1);

                            dataList.add(builder
                                    .deleteMarkAttr(false)
                                    .idAttr(id)
                                    .lastChangeAttr(
                                            new DateTime()
                                                    .withMillisOfSecond(0))
                                    .lastUserAttr(null)
                                    .attr("f1", true)
                                    .attr("f2", i.toString())
                                    .attr("ORG_ID", i.toString())
                                    .attr("ORG_ROLE_ID", i.toString())
                                    .build());
                        }
                        for (DictRow data : dataList) {
                            dao.setParamsForInsert(query, data, ps);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testListDict1SourceQueryWithParams() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery insertQuery = dict.query().addAttrs();
                    insertDict1Row(connection, insertQuery, "v1");
                    insertDict1Row(connection, insertQuery, "v2");
                    insertDict1Row(connection, insertQuery, "v3");

                    NsiQuery query = dict.query().addAttr("f1");
                    List<DictRow> dataList = sqlDao.list(connection, query, null,
                            dict.sort().add("f1").build() ,
                            -1, -1,
                            "TEST2",
                            dict.params().add(MetaFieldType.VARCHAR, "v2").build() );
                    Assert.assertEquals(2, dataList.size());
                    DataUtils.assertEquals(query, dict.builder().attr("f1", "v1").build(), dataList.get(0), true);
                    DataUtils.assertEquals(query, dict.builder().attr("f1", "v3").build(), dataList.get(1), true);

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testCountDict1SourceQueryWithParams() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery insertQuery = dict.query().addAttrs();
                    insertDict1Row(connection, insertQuery, "v1");
                    insertDict1Row(connection, insertQuery, "v2");
                    insertDict1Row(connection, insertQuery, "v3");

                    NsiQuery query = dict.query().addAttr("f1");
                    Assert.assertEquals(2, sqlDao.count(connection, query, null,
                            "TEST2",
                            dict.params().add(MetaFieldType.VARCHAR, "v2").build() ));
                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }


    @Test
    public void testViewQueryWithParams() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery insertQuery = dict.query().addAttrs();
                    insertDict1Row(connection, insertQuery, "v1");
                    insertDict1Row(connection, insertQuery, "v2");
                    insertDict1Row(connection, insertQuery, "v3");

                    NsiConfigDict viewDict = config.getDict("dict1_view");

                    NsiQuery query = viewDict.query().addAttrs();

                    List<DictRow> dataList = sqlDao.list(connection, query, null,
                            viewDict.sort().add("f1").build() ,
                            -1, -1,
                            "QUERY1",
                            viewDict.params().add(MetaFieldType.VARCHAR, "v2").build() );
                    Assert.assertEquals(2, dataList.size());
                    DataUtils.assertEquals(query, viewDict.builder()
                            .attr("f1", "v1")
                            .attr("cnt", 1L)
                            .build(), dataList.get(0), true);
                    DataUtils.assertEquals(query, viewDict.builder()
                            .attr("f1", "v3")
                            .attr("cnt", 1L)
                            .build(), dataList.get(1), true);
                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

}
