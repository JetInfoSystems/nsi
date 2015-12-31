package jet.isur.nsi.services.sql.test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.BoolExpBuilder;
import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.DictRowBuilder;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.tx.NsiTransaction;
import jet.isur.nsi.api.tx.NsiTransactionService;
import jet.isur.nsi.api.tx.NsiTransactionTemplate;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.migrator.platform.PlatformMigrator;
import jet.isur.nsi.migrator.platform.oracle.OraclePlatformMigrator;
import jet.isur.nsi.services.NsiTransactionServiceImpl;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import jet.isur.nsi.testkit.utils.DataUtils;
import jet.scdp.metrics.mock.MockMetrics;

public class SqlDaoTest extends BaseSqlTest {

    protected PlatformMigrator platformMigrator;
    private static final Logger log = LoggerFactory.getLogger(SqlDaoTest.class);
    
    @Override
    public void setup() throws Exception {
        platformMigrator = new OraclePlatformMigrator();
        platform = platformMigrator.getPlatform();
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("src/test/resources/metadata1"), configParams ).getConfig();
    }

    @Test
    public void testInsertAndGet() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    DictRow outData = insertDict1Row(connection, query, "f1-value");
                    Assert.assertEquals(1L, (long) outData.getIdAttrLong());

                    DictRow getData = sqlDao.get(connection, query, outData.getIdAttr());
                    DataUtils.assertEquals(query, outData, getData);

                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testInsertAndGetView() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        NsiConfigDict dictV = config.getDict("dict1_v");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    DictRow inData = query.getDict().builder()
                            .deleteMarkAttr(false)
                            .idAttrNull()
                            .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                            .lastUserAttr(null)
                            .attr("V", 1L)
                            .attr("f1", "test")
                            .attr("ORG_ID", 1L)
                            .attr("ORG_ROLE_ID", 2L)
                            .build();

                    DictRow outData = sqlDao.insert(connection, query, inData);

                    DictRow outDataV = sqlDao.get(connection, dictV.query().addAttrs(), outData.getIdAttr());
                    
                    Assert.assertEquals(1L, (long) outDataV.getIdAttrLong());
                    Assert.assertEquals(123L, (long) outDataV.getLong("V"));
                    
                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testInsertAndGetFloatNumber() throws Exception {
        NsiConfigDict dict = config.getDict("dict5");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    DictRow inData = query.getDict().builder()
                            .deleteMarkAttr(false)
                            .idAttrNull()
                            .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                            .lastUserAttr(null)
                            .attr("f1", "63.21474351071076")
                            .build();

                    DictRow outData = sqlDao.insert(connection, query, inData);

                    Assert.assertEquals(new Double(63.21474), outData.getDouble("f1"));

                    DictRow getData = sqlDao.get(connection, query, outData.getIdAttr());
                    DataUtils.assertEquals(query, outData, getData);

                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertAndGetClob() throws Exception {
        NsiConfigDict dict = config.getDict("dictWithClob");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    String clobValue = "test insert clob";
                    NsiQuery query = dict.query().addAttrs();
                    DictRow inData = query.getDict().builder()
                            .deleteMarkAttr(false)
                            .idAttrNull()
                            .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                            .lastUserAttr(null)
                            .attr("clobAttr", clobValue)
                            .build();

                    DictRow outData = sqlDao.insert(connection, query, inData);

                    Assert.assertEquals(clobValue, outData.getString("clobAttr"));

                    DictRow getData = sqlDao.get(connection, query, outData.getIdAttr());
                    DataUtils.assertEquals(query, outData, getData);

                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }

    }

    @Test
    public void testInsertAndGetWithRefFields() throws Exception {
        NsiConfigDict dict1 = config.getDict("dict1");
        NsiConfigDict dict2 = config.getDict("dict2");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict2, connection);
            platformMigrator.recreateTable(dict1, connection);
            try {
                platformMigrator.recreateSeq(dict2, connection);
                platformMigrator.recreateSeq(dict1, connection);
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
                    platformMigrator.dropSeq(dict2, connection);
                    platformMigrator.dropSeq(dict1, connection);
                }

            } finally {
                platformMigrator.dropTable(dict2, connection);
                platformMigrator.dropTable(dict1, connection);
            }
        }
    }

    @Test
    public void testInsertAndUpdate() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    DictRow outData = insertDict1Row(connection, query, "f1-value");

                    DictRow inUpdatedData = dict.builder(
                            DictRowBuilder.cloneRow(outData)).attr("f1","f1-value-changed").build();

                    DictRow controlData = DictRowBuilder.cloneRow(inUpdatedData);

                    DictRow outUpdatedData = sqlDao.update(connection, query, inUpdatedData);
                    DataUtils.assertEquals(query, controlData, outUpdatedData);

                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testList() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    for (int i = 0; i < 10; i++) {
                        insertDict1Row(connection, query, "value" + i);
                    }

                    List<DictRow> rows = sqlDao.list(connection, query, null, null, -1, -1);

                    Assert.assertEquals(10, rows.size());

                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testListReverseSort() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
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
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testListFilterSort() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
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
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testListContainsFilter() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        String fullSearchFName = "f1";
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    platformMigrator.recreateFullSearchIndex(dict, fullSearchFName, connection);
                    try {
                        NsiQuery query = dict.query().addAttrs();
                        for (int i = 0; i < 10; i++) {
                            if (i % 2 == 0) {
                                insertDict1Row(connection, query, "value" + i);
                            } else {
                                insertDict1Row(connection, query, "any" + i);
                            }
                        }

                        List<DictRow> rows = sqlDao.list(connection, query,
                                dict.filter().key(fullSearchFName).contains()
                                        .value("alu").build(), null, -1, -1);

                        Assert.assertEquals(5, rows.size());
                    } finally {
                        platformMigrator.dropFullSearchIndex(dict, fullSearchFName, connection);
                    }
                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testCountFilter() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    for (int i = 0; i < 10; i++) {
                        insertDict1Row(connection, query, "value" + i);
                    }


                    long count = sqlDao.count(connection, query,
                            dict.filter().key("f1").eq().value("value5").build());

                    Assert.assertEquals(1L, count);

                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
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
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
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
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }


    @Test
    public void testBatchInsert() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {

                platformMigrator.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    // ID будем задавать явно, поэтому последовательность не
                    // используем
                    String sql = sqlGen.getRowInsertSql(query, false);
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
                            sqlDao.setParamsForInsert(query, data, ps);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                } finally {
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testListDict1SourceQueryWithParams() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
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
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testCountDict1SourceQueryWithParams() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
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
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }


    @Test
    public void testViewQueryWithParams() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            platformMigrator.recreateTable(dict, connection);
            try {
                platformMigrator.recreateSeq(dict, connection);
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
                    platformMigrator.dropSeq(dict, connection);
                }

            } finally {
                platformMigrator.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testMerge() throws SQLException {
        NsiConfigDict dictEmp = config.getDict("EMP_MERGE_TEST");
        NsiConfigDict dictOrg = config.getDict("ORG_MERGE_TEST");
        
        try (Connection connection = dataSource.getConnection()) {
            try {
                platformMigrator.recreateTable(dictOrg, connection);
                platformMigrator.recreateTable(dictEmp, connection);            
                platformMigrator.recreateSeq(dictEmp, connection);
                platformMigrator.recreateSeq(dictOrg, connection);
                
                DictRow org1 = defaultBuilder("ORG_MERGE_TEST").attr("EXTERNAL_ID", "1").build();
                DictRow org2 = defaultBuilder("ORG_MERGE_TEST").attr("EXTERNAL_ID", "2").build();
                
                sqlDao.mergeByExternalAttrs(connection, org1);
                List<DictRow> rows = sqlDao.list(connection, dictOrg.query().addAttrs(), getFilterByExternalAttrs(org1), null, -1, -1);
                Assert.assertEquals(rows.size(), 1);
                DictRowAttr org1Id = rows.get(0).getIdAttr();
                long count = sqlDao.count(connection, dictOrg.query().addAttrs(), null, null, null);
                Assert.assertEquals(count, 1);
                
                sqlDao.mergeByExternalAttrs(connection, org1);
                count = sqlDao.count(connection, dictOrg.query().addAttrs(), null, null, null);
                Assert.assertEquals(count, 1);
                
                sqlDao.mergeByExternalAttrs(connection, org2);
                rows = sqlDao.list(connection, dictOrg.query().addAttrs(), getFilterByExternalAttrs(org1), null, -1, -1);
                Assert.assertEquals(rows.size(), 1);
                DictRowAttr org2Id = rows.get(0).getIdAttr();
                count = sqlDao.count(connection, dictOrg.query().addAttrs(), null, null, null);
                Assert.assertEquals(count, 2);
                
                
                DictRow emp1 = defaultBuilder("EMP_MERGE_TEST").attr("EXTERNAL_ID", "1").build();
                Map<String, DictRowAttr> ref = new HashMap<>();
                ref.put("EXTERNAL_ID", org1.getAttr("EXTERNAL_ID"));
                emp1.getAttr("ORG_ID").setRefAttrs(ref);
                emp1.getAttr("ORG_ID").setValues(null);
                sqlDao.mergeByExternalAttrs(connection, emp1);
                rows = sqlDao.list(connection, dictEmp.query().addAttrs(), getFilterByExternalAttrs(emp1), null, -1, -1);
                Assert.assertEquals(rows.size(), 1);
                Assert.assertEquals(rows.get(0).getAttr("ORG_ID").getLong(), org1Id.getLong());
                
            } finally {
                platformMigrator.dropSeq(dictEmp, connection);
                platformMigrator.dropSeq(dictOrg, connection);
                platformMigrator.dropTable(dictEmp, connection);   
                platformMigrator.dropTable(dictOrg, connection);        
            }
        }
    }
    
    @Test
    public void testUniqueAttr() {
        new NsiTransactionTemplate<Void>(getTransactionService(), "1", log) {

            @Override
            public Void doInTransaction(NsiTransaction tx) {
                config = getNsiConfig("/opt/isur/metadata");
                String key = String.valueOf(System.nanoTime());
                NsiConfigDict dictEventCat = config.getDict("EVENT_CATEGORY");
                DictRow eventCat1 = defaultBuilder("EVENT_CATEGORY").attr("EVENT_CATEGORY_KEY", key).build();
                DictRow eventCat2 = defaultBuilder("EVENT_CATEGORY").attr("EVENT_CATEGORY_KEY", key + 2).build();
                DictRow eventCatEmpty = defaultBuilder("EVENT_CATEGORY").build();

                DictRow res1 = sqlDao.save(tx.getConnection(), dictEventCat.query().addAttrs(), eventCat1, true);
                DictRow res2 = sqlDao.save(tx.getConnection(), dictEventCat.query().addAttrs(), eventCat2, true);

                sqlDao.save(tx.getConnection(), dictEventCat.query().addAttrs(), res1, false);
                res1.removeAttr("EVENT_CATEGORY_KEY");
                sqlDao.save(tx.getConnection(), dictEventCat.query().addAttrs(), res1, false);

                eventCat1.removeAttr("ID");
                try {
                    sqlDao.save(tx.getConnection(), dictEventCat.query().addAttrs(), eventCat1, true);
                    Assert.assertTrue(false);
                } catch (NsiServiceException e) {
                }
                try {
                    sqlDao.save(tx.getConnection(), dictEventCat.query().addAttrs(), eventCatEmpty, true);
                    Assert.assertTrue(false);
                } catch (NsiServiceException e) {
                }
                try {
                    res1.setAttr("EVENT_CATEGORY_KEY", res2.getAttr("EVENT_CATEGORY_KEY"));
                    sqlDao.save(tx.getConnection(), dictEventCat.query().addAttrs(), res1, false);
                    Assert.assertTrue(false);
                } catch (NsiServiceException e) {
                }
                
                tx.rollback();
                return null;
            }
            
        }.start();
    }
    
    public NsiTransactionService getTransactionService() {
        NsiTransactionServiceImpl transactionService = new NsiTransactionServiceImpl(new MockMetrics());       
        transactionService.setDataSource(dataSource); 
        
        return transactionService;
    }
    
    private BoolExp getFilterByExternalAttrs(DictRow row) {
        NsiConfigDict dict = row.getDict();
        List<NsiConfigAttr> mAttrs = dict.getMergeExternalAttrs();
        BoolExpBuilder fb = row.getDict().filter().and().expList();
        if(dict.getOwnerAttr() != null && row.getOwnerAttr() != null) {
            fb.key(dict.getOwnerAttr().getName()).eq().value(row.getOwnerAttr()).add();
        }
        for(NsiConfigAttr configAttr : mAttrs) {
            DictRowAttr rowAttr = row.getAttr(configAttr.getName());
            Preconditions.checkNotNull(rowAttr, "Атрибут %s не существует в %s", configAttr.getName(), dict.getName()) ;
            fb.key(configAttr.getName()).eq().value(rowAttr).add();
        }
        return fb.end().build();
    }
}
