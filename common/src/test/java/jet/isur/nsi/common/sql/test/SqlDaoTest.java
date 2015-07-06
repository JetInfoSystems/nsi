package jet.isur.nsi.common.sql.test;

import java.sql.Connection;
import java.util.List;

import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowAttrBuilder;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.builder.BoolExpBuilder;
import jet.isur.nsi.api.model.builder.SortListBuilder;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.utils.DaoUtils;
import jet.isur.nsi.common.utils.DataUtils;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class SqlDaoTest extends BaseSqlTest {

    private DefaultSqlDao sqlDao;

    @Override
    public void setup() throws Exception {
        super.setup();
        sqlDao = new DefaultSqlDao();
        sqlDao.setSqlGen(sqlGen);
    }

    @Test
    public void testInsertAndGet() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(config, dict).addAttrs();
                    DictRow outData = insertDict1Row(connection, query, "f1-value");
                    DictRowBuilder outDataBuilder = new DictRowBuilder(query, outData);
                    Long idValue = outDataBuilder.getLongIdAttr();
                    Assert.assertEquals(1L, (long) idValue);

                    DictRow getData = sqlDao.get(connection, query,
                            outDataBuilder.getIdAttr());
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
            DaoUtils.createTable(dict1, connection);
            DaoUtils.createTable(dict2, connection);
            try {
                DaoUtils.createSeq(dict1, connection);
                DaoUtils.createSeq(dict2, connection);
                try {
                    NsiQuery query1 = new NsiQuery(config, dict1).addAttrs();
                    DictRow dict1Data = insertDict1Row(connection, query1, "f1-value");

                    NsiQuery query2 = new NsiQuery(config, dict2).addAttrs();
                    long dict1Id = new DictRowBuilder(query1, dict1Data).getLongIdAttr();
                    DictRow dict2Data = saveDict2Row(connection, query2, dict1Id, true);

                    NsiQuery query = new NsiQuery(config, dict2).addAttrs();

                    DictRow getData = sqlDao.get(connection, query, new DictRowBuilder(query2, dict2Data).getIdAttr());
                    DataUtils.assertEquals(query, dict2Data, getData);


                } finally {
                    DaoUtils.dropSeq(dict1, connection);
                    DaoUtils.dropSeq(dict2, connection);
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
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(config, dict).addAttrs();
                    DictRow outData = insertDict1Row(connection, query, "f1-value");

                    DictRow inUpdatedData = new DictRowBuilder(query,
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
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(config, dict).addAttrs();
                    for (int i = 0; i < 100; i++) {
                        DictRow outData = insertDict1Row(connection, query, "value" + i);
                    }

                    List<DictRow> rows = sqlDao.list(connection, query, null, null, -1, -1);

                    Assert.assertEquals(100, rows.size());

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
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(config, dict).addAttrs();
                    for (int i = 0; i < 100; i++) {
                        DictRow outData = insertDict1Row(connection, query, "value" + i);
                    }


                    List<DictRow> rows = sqlDao.list(connection, query,
                            null,
                            new SortListBuilder().add("f1", false).build(), -1, -1);

                    Assert.assertEquals(100, rows.size());
                    Assert.assertEquals("value99", new DictRowBuilder(query, rows.get(0)).getString("f1"));

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
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(config, dict).addAttrs();
                    for (int i = 0; i < 100; i++) {
                        DictRow outData = insertDict1Row(connection, query, "value" + i);
                    }


                    List<DictRow> rows = sqlDao.list(connection, query,
                            new BoolExpBuilder().key("f1").func("=")
                            .value(DictRowAttrBuilder.build("value50")).build(),
                            new SortListBuilder().add("f1", false).build(), -1, -1);

                    Assert.assertEquals(1, rows.size());
                    Assert.assertEquals("value50", new DictRowBuilder(query, rows.get(0)).getString("f1"));

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
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(config, dict).addAttrs();
                    for (int i = 0; i < 100; i++) {
                        DictRow outData = insertDict1Row(connection, query, "value" + i);
                    }


                    long count = sqlDao.count(connection, query,
                            new BoolExpBuilder().key("f1").func("=")
                            .value(DictRowAttrBuilder.build("value50")).build());

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
        DictRow inData = new DictRowBuilder(query)
                .deleteMarkAttr(false)
                .idAttr(null)
                .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                .lastUserAttr(null)
                .attr("f1", f1Value)
                .build();

        DictRow outData = sqlDao.insert(connection, query, inData);
        return outData;
    }

    private DictRow saveDict2Row(Connection connection, NsiQuery query,
            long  dic1Id, boolean insert) {
        DictRow inData = new DictRowBuilder(query)
                .deleteMarkAttr(false)
                .idAttr(null)
                .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                .lastUserAttr(null)
                .attr("f1", "test")
                .attr("dict1_id", dic1Id)
                .build();

        DictRow outData = sqlDao.save(connection, query, inData, insert);
        return outData;
    }

    @Test
    public void testInsertAndGetChar1Char2() throws Exception {
        NsiConfigDict dict = config.getDict("dict4");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(config, dict).addAttrs();
                    DictRow inData = new DictRowBuilder(query)
                        .deleteMarkAttr(false)
                        .idAttr(null)
                        .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                        .lastUserAttr(null)
                        .attr("f1", true)
                        .attr("f2", "A")
                        .build();

                    DictRow outData = sqlDao.insert(connection, query, inData);
                    DictRowBuilder outDataBuilder = new DictRowBuilder(query, outData);
                    Long idValue = outDataBuilder.getLongIdAttr();
                    Assert.assertEquals(1L, (long) idValue);
                    Assert.assertEquals(true, outDataBuilder.getBool("f1"));
                    Assert.assertEquals("A", outDataBuilder.getString("f2"));

                    DictRow getData = sqlDao.get(connection, query,
                            outDataBuilder.getIdAttr());
                    DataUtils.assertEquals(query, outData, getData);

                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

}
