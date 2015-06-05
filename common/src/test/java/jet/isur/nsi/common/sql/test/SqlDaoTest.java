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
import jet.isur.nsi.common.sql.SqlDao;
import jet.isur.nsi.common.utils.DaoUtils;
import jet.isur.nsi.common.utils.DataUtils;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class SqlDaoTest extends BaseSqlTest {

    private SqlDao sqlDao;

    @Override
    public void setup() throws Exception {
        super.setup();
        sqlDao = new SqlDao();
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
                    NsiQuery query = new NsiQuery(dict).addAttrs();
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
    public void testInsertAndUpdate() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.createTable(dict, connection);
            try {
                DaoUtils.createSeq(dict, connection);
                try {
                    NsiQuery query = new NsiQuery(dict).addAttrs();
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
                    NsiQuery query = new NsiQuery(dict).addAttrs();
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
                    NsiQuery query = new NsiQuery(dict).addAttrs();
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
                    NsiQuery query = new NsiQuery(dict).addAttrs();
                    for (int i = 0; i < 100; i++) {
                        DictRow outData = insertDict1Row(connection, query, "value" + i);
                    }


                    List<DictRow> rows = sqlDao.list(connection, query,
                            new BoolExpBuilder().key("f1").func("=")
                            .value(DictRowAttrBuilder.build("f1", "value50")).build(),
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
                    NsiQuery query = new NsiQuery(dict).addAttrs();
                    for (int i = 0; i < 100; i++) {
                        DictRow outData = insertDict1Row(connection, query, "value" + i);
                    }


                    long count = sqlDao.count(connection, query,
                            new BoolExpBuilder().key("f1").func("=")
                            .value(DictRowAttrBuilder.build("f1", "value50")).build());

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
        DictRow inData = new DictRowBuilder(query).deleteMarkAttr(false)
                .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                .lastUserAttr(null).attr("f1", f1Value).build();

        DictRow outData = sqlDao.insert(connection, query, inData);
        return outData;
    }

}
