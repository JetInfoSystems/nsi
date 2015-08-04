package jet.isur.nsi.common.sql.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowAttrBuilder;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.OperationType;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.api.model.builder.BoolExpBuilder;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.common.sql.DefaultSqlGen;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import junit.framework.Assert;

import org.junit.Test;

public class SqlGenTest extends BaseSqlTest {

    private NsiConfig config;
    private DefaultSqlGen sqlGen;

    @Override
    public void setup() throws Exception {
        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("src/test/resources/metadata1"), configParams ).getConfig();
        sqlGen = new DefaultSqlGen();
    }

    @Test
    public void testDict1RowGetSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        String sql = sqlGen.getRowGetSql(query);
        Assert.assertEquals(
                "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user "
                        + "from table1 m " + "where m.id = ?", sql);
    }

    @Test
    public void testDict2RowGetSql() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        String sql = sqlGen.getRowGetSql(query);
        Assert.assertEquals(
                "select m.f1, m.dict1_id, a1.f1 a1_f1, m.id, m.last_change, m.is_deleted, m.last_user "
                        + "from table2 m "
                        + "left outer join table1 a1 on m.dict1_id = a1.id "
                        + "where m.id = ?", sql);
    }

    @Test
    public void testDict3RowGetSql() {
        NsiConfigDict dict = config.getDict("dict3");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        String sql = sqlGen.getRowGetSql(query);
        Assert.assertEquals(
                "select m.f1, m.dict1_id, a1.f1 a1_f1, m.dict1_a_id, a2.f1 a2_f1, m.id, m.last_change, m.is_deleted, m.last_user "
                        + "from table3 m "
                        + "left outer join table1 a1 on m.dict1_id = a1.id "
                        + "left outer join table1 a2 on m.dict1_a_id = a2.id "
                        + "where m.id = ?", sql);
    }

    @Test
    public void testDict1ListSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        BoolExp filter = new BoolExpBuilder()
            .key("f1")
            .func(OperationType.EQUALS)
            .value(DictRowAttrBuilder.from("1"))
            .build();

        List<SortExp> sortList = new ArrayList<>();
        sortList.add(buildSortExp("id", true));
        sortList.add(buildSortExp("last_user", true));

        String sql = sqlGen.getListSql(query, filter, sortList, 1, 2);
        Assert.assertEquals(
                "select * from (select a.*, ROWNUM rnum from (" +
                        "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user from table1 m" +
                        " where m.f1 = ? order by m.id asc, m.last_user asc" +
                        ") a where ROWNUM < ?) b where rnum >= ?", sql);
    }

    @Test
    public void testDict1ListComplexFilterSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        BoolExp f1Filter = new BoolExpBuilder()
            .func(OperationType.AND)
            .expList()
                .key("f1").func("=").value(DictRowAttrBuilder.from("1")).add()
                .key("is_deleted").func(OperationType.EQUALS).value(DictRowAttrBuilder.from(true)).add()
            .end()
            .build();

        List<SortExp> sortList = new ArrayList<>();
        sortList.add(buildSortExp("id", true));
        sortList.add(buildSortExp("last_user", true));

        String sql = sqlGen.getListSql(query, f1Filter, sortList, 1, 2);
        Assert.assertEquals(
                "select * from (select a.*, ROWNUM rnum from (" +
                        "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user from table1 m" +
                        " where (m.f1 = ? and m.is_deleted = ?) order by m.id asc, m.last_user asc" +
                        ") a where ROWNUM < ?) b where rnum >= ?", sql);
    }

    @Test
    public void testDict3ListSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        BoolExp filter = new BoolExpBuilder()
            .key("f1")
            .func(OperationType.LIKE)
            .value(DictRowAttrBuilder.from("1"))
            .build();

        List<SortExp> sortList = new ArrayList<>();
        sortList.add(buildSortExp("id", true));
        sortList.add(buildSortExp("last_user", true));

        String sql = sqlGen.getListSql(query, filter, sortList, 1, 2);
        Assert.assertEquals(
                "select * from (select a.*, ROWNUM rnum from (" +
                        "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user from table1 m" +
                        " where m.f1 like ? order by m.id asc, m.last_user asc" +
                        ") a where ROWNUM < ?) b where rnum >= ?", sql);
    }
    /*
     * @Test public void testDict2ListSql() { NsiConfigDict dict =
     * config.getDict("dict1"); NsiQuery query = new NsiQuery(dict).addAttrs();
     * BoolExp filter = new BoolExp(); filter.setFunc("or");
     *
     * BoolExp e1 = new BoolExp(); e1.setFunc("="); e1.setKey("f1");
     *
     * BoolExp e2 = new BoolExp(); e2.setFunc("="); e2.setKey("f1");
     *
     * List<BoolExp> expList = new ArrayList<BoolExp>() ; expList.add(e1);
     * expList.add(e2); filter.setExpList(expList );
     *
     * List<SortExp> sortList = new ArrayList<>();
     * sortList.add(buildSortExp("id",true));
     * sortList.add(buildSortExp("last_user",true));
     *
     * String sql = sqlGen.getListSql(query, filter ,sortList, 1, 2 );
     * Assert.assertEquals(
     * "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user " +
     * "from table1 m " + "where (m.f1 = ? or m.f1 = ?)" +
     * "order by m.id asc, m.last_user asc limit ?", sql); }
     */

    private SortExp buildSortExp(String key, boolean asc) {
        SortExp result = new SortExp();
        result.setKey(key);
        result.setAsc(asc);
        return result;
    }

    @Test
    public void testDict1CountSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        BoolExp filter = new BoolExpBuilder()
        .key("f1")
        .func(OperationType.EQUALS)
        .value(DictRowAttrBuilder.from("1"))
        .build();

        String sql = sqlGen.getCountSql(query, filter);
        Assert.assertEquals("select count(*) " + "from table1 m "
                + "where m.f1 = ?", sql);
    }


    @Test
    public void testDict1RowInsertSeq() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();

        String sql = sqlGen.getRowInsertSql(query, true);
        Assert.assertEquals(
                "insert into table1 (f1, id, is_deleted, last_change, last_user) "
                        + "values (?, seq_table1.nextval, ?, ?, ?)", sql);

    }

    @Test
    public void testDict1RowInsertWithoutSeq() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();

        String sql = sqlGen.getRowInsertSql(query, false);
        Assert.assertEquals(
                "insert into table1 (f1, id, is_deleted, last_change, last_user) "
                        + "values (?, ?, ?, ?, ?)", sql);

    }

    @Test
    public void testDict2RowInsertSeq() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();

        String sql = sqlGen.getRowInsertSql(query, true);
        Assert.assertEquals(
                "insert into table2 (f1, dict1_id, id, last_change, is_deleted, last_user) "
                        + "values (?, ?, seq_table2.nextval, ?, ?, ?)", sql);

    }

    @Test
    public void testDict2RowUpdateSeq() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();

        String sql = sqlGen.getRowUpdateSql(query);
        Assert.assertEquals(
                "update table2 m "
                        + "set m.f1 = ?, m.dict1_id = ?, m.last_change = ?, m.is_deleted = ?, m.last_user = ? "
                        + "where m.id = ?", sql);

    }

    @Test
    public void testOperationsSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        BoolExp filter = new BoolExpBuilder()
        .func(OperationType.AND)
        .expList()
            .key("f1").func(OperationType.EQUALS).value(DictRowAttrBuilder.from("1")).add()
            .key("f1").func(OperationType.LT).value(DictRowAttrBuilder.from("1")).add()
            .key("f1").func(OperationType.LE).value(DictRowAttrBuilder.from("1")).add()
            .func(OperationType.NOTAND)
            .expList()
                .key("f1").func(OperationType.GT).value(DictRowAttrBuilder.from("1")).add()
                .key("f1").func(OperationType.GE).value(DictRowAttrBuilder.from("1")).add()
            .end().add()
            .func(OperationType.OR)
            .expList()
                .key("f1").func(OperationType.GT).value(DictRowAttrBuilder.from("1")).add()
                .key("f1").func(OperationType.GE).value(DictRowAttrBuilder.from("1")).add()
            .end().add()
            .func(OperationType.NOTOR)
            .expList()
                .key("f1").func(OperationType.GT).value(DictRowAttrBuilder.from("1")).add()
                .key("f1").func(OperationType.GE).value(DictRowAttrBuilder.from("1")).add()
            .end().add()
        .end()
        .build();

        String sql = sqlGen.getCountSql(query, filter);
        Assert.assertEquals(
                "select count(*) "
                + "from table1 m "
                + "where (m.f1 = ? and m.f1 < ? and m.f1 <= ? "
                + "and not((m.f1 > ? and m.f1 >= ?)) "
                + "and (m.f1 > ? or m.f1 >= ?) "
                + "and not((m.f1 > ? or m.f1 >= ?)))", sql);
    }

}
