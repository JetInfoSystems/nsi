package jet.nsi.services.sql.test.oracle;

import java.io.File;

import org.junit.Test;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.migrator.platform.oracle.OraclePlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.OraclePlatformDaoUtils;
import junit.framework.Assert;

public class SqlGenTest extends BaseSqlTest {
    private static final String DB_IDENT = "nsi.oracle";
    
    private NsiConfig config;
    private PlatformMigrator platformMigrator;

    public SqlGenTest() {
        super(DB_IDENT, new OraclePlatformDaoUtils());
    }
    
    @Override
    public void setup() throws Exception {
        super.setup();

        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("src/test/resources/metadata1"), configParams ).getConfig();
    }

    @Override
    protected void initPlatformSpecific() {
        platformMigrator = new OraclePlatformMigrator(params);
        platform = platformMigrator.getPlatform();
    }

    @Test
    public void testDict1RowGetSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getRowGetSql(query);
        Assert.assertEquals(
                "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.ORG_ID, m.ORG_ROLE_ID, m.VERSION "
                        + "from table1 m " + "where m.id = ?", sql);
    }

    @Test
    public void testDict2RowGetSql() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getRowGetSql(query);
        Assert.assertEquals(
                "select m.dict1_id, a1.f1 a1_f1, m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.VERSION "
                        + "from table2 m "
                        + "left outer join table1 a1 on m.dict1_id = a1.id "
                        + "where m.id = ?", sql);
    }

    @Test
    public void testDict2RowGetForUpdateSql() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getRowGetSql(query, true);
        Assert.assertEquals(
                "select m.dict1_id, m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.VERSION from table2 m where m.id = ? for update", sql);
    }

    @Test
    public void testDict3RowGetSql() {
        NsiConfigDict dict = config.getDict("dict3");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getRowGetSql(query);
        Assert.assertEquals(
                "select m.dict1_a_id, a1.f1 a1_f1, m.dict1_id, a2.f1 a2_f1, m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.VERSION "
                        + "from table3 m "
                        + "left outer join table1 a1 on m.dict1_a_id = a1.id "
                        + "left outer join table1 a2 on m.dict1_id = a2.id "
                        + "where m.id = ?", sql);
    }

    @Test
    public void testDict1ListSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();


        String sql = sqlGen.getListSql(query,
                dict.filter()
                    .key("f1").eq().value(1)
                    .build(),
                dict.sort()
                    .add(dict.getIdAttr())
                    .add(dict.getLastUserAttr())
                    .build(), 1, 2);
        Assert.assertEquals(
                "select b.* from (select a.*, ROWNUM rnum from (" +
                        "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.ORG_ID, m.ORG_ROLE_ID, m.VERSION from table1 m" +
                        " where m.f1 = ? order by m.id asc, m.last_user asc" +
                        ") a where ROWNUM < ?) b where rnum >= ?", sql);
    }

    @Test
    public void testDict1ListComplexFilterSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getListSql(query,
            dict.filter()
                .and()
                .expList()
                    .key("f1").eq().value(1).add()
                    .deleteMark(true).add()
                .end()
                .build(),
            dict.sort()
                .add(dict.getIdAttr())
                .add(dict.getLastUserAttr())
                .build(), 1, 2);
        Assert.assertEquals(
                "select b.* from (select a.*, ROWNUM rnum from (" +
                        "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.ORG_ID, m.ORG_ROLE_ID, m.VERSION from table1 m" +
                        " where (m.f1 = ? and m.is_deleted = ?) order by m.id asc, m.last_user asc" +
                        ") a where ROWNUM < ?) b where rnum >= ?", sql);
    }

    @Test
    public void testDict3ListSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getListSql(query,
                dict.filter()
                    .key("f1").like().value(1)
                    .build(),
                dict.sort()
                    .add(dict.getIdAttr())
                    .add(dict.getLastUserAttr())
                    .build(), 1, 2);
        Assert.assertEquals(
                "select b.* from (select a.*, ROWNUM rnum from (" +
                        "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.ORG_ID, m.ORG_ROLE_ID, m.VERSION from table1 m" +
                        " where lower(m.f1) like lower(?) order by m.id asc, m.last_user asc" +
                        ") a where ROWNUM < ?) b where rnum >= ?", sql);
    }

    @Test
    public void testDict4ListSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getListSql(query, dict.filter()
            .key("f1").notNull().build(), null, -1, -1);
        Assert.assertEquals(
                "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.ORG_ID, m.ORG_ROLE_ID, m.VERSION from table1 m" +
                        " where m.f1 is not null", sql);
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

    @Test
    public void testDict1CountSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();
        String sql = sqlGen.getCountSql(query, dict.filter().key("f1").eq().value(1).build());
        Assert.assertEquals("select count(*) " + "from table1 m "
                + "where m.f1 = ?", sql);
    }


    @Test
    public void testDict1RowInsertSeq() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();

        String sql = sqlGen.getRowInsertSql(query, true);
        Assert.assertEquals(
                "insert into table1 (f1, id, is_deleted, last_change, last_user, ORG_ID, ORG_ROLE_ID, VERSION) "
                        + "values (?, seq_table1.nextval, ?, ?, ?, ?, ?, ?)", sql);

    }

    @Test
    public void testDict1RowInsertWithoutSeq() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();

        String sql = sqlGen.getRowInsertSql(query, false);
        Assert.assertEquals(
                "insert into table1 (f1, id, is_deleted, last_change, last_user, ORG_ID, ORG_ROLE_ID, VERSION) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?)", sql);

    }

    @Test
    public void testDict2RowInsertSeq() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = dict.query().addAttrs();

        String sql = sqlGen.getRowInsertSql(query, true);
        Assert.assertEquals(
                "insert into table2 (dict1_id, f1, id, is_deleted, last_change, last_user, VERSION) "
                        + "values (?, ?, seq_table2.nextval, ?, ?, ?, ?)", sql);

    }

    @Test
    public void testDict2RowUpdateSeq() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = dict.query().addAttrs();

        String sql = sqlGen.getRowUpdateSql(query);
        Assert.assertEquals(
                "update table2 "
                        + "set dict1_id = ?, f1 = ?, is_deleted = ?, last_change = ?, last_user = ?, VERSION = ? "
                        + "where id = ?", sql);
    }

    @Test
    public void testDict2RowUpdateSeqWithoutVersion() {
        NsiConfigDict dict = config.getDict("dict2_without_version");
        NsiQuery query = dict.query().addAttrs();

        String sql = sqlGen.getRowUpdateSql(query);
        Assert.assertEquals(
                "update table2 "
                        + "set dict1_id = ?, f1 = ?, is_deleted = ?, last_change = ?, last_user = ? "
                        + "where id = ?", sql);
    }

    @Test
    public void testContainsOperationSql(){
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();
        BoolExp filter = dict.filter()
                .key("f1").contains().value("abc")
                .build();
        String sql = sqlGen.getListSql(query, filter, null, -1, -1);
        Assert.assertEquals(
                "select m.f1, m.id, m.is_deleted, m.last_change, m.last_user, m.ORG_ID, m.ORG_ROLE_ID, m.VERSION from table1 m where catsearch(m.f1, ?, '') > 0", sql);
    }
    
    @Test
    public void testOperationsSql() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();
        BoolExp filter = dict.filter()
                .and()
                .expList()
                    .key("f1").eq().value(1).add()
                    .key("f1").lt().value(1).add()
                    .key("f1").le().value(1).add()
                    .notAnd()
                    .expList()
                        .key("f1").gt().value(1).add()
                        .key("f1").ge().value(1).add()
                    .end().add()
                    .or()
                    .expList()
                        .key("f1").gt().value(1).add()
                        .key("f1").ge().value(1).add()
                    .end().add()
                    .notOr()
                    .expList()
                        .key("f1").gt().value(1).add()
                        .key("f1").ge().value(1).add()
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

    @Test
    public void testCountFromSourceQuery() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();

        String sql = sqlGen.getCountSql(query, null, "TEST1");
        Assert.assertEquals(
                "select count(*) from ( select f1 from table1 )  m", sql);

    }

    @Test
    public void testListFromSourceQuery() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttr("f1");

        String sql = sqlGen.getListSql(query, null, null, -1, -1, "TEST1");
        Assert.assertEquals(
                "select m.f1 from ( select f1 from table1 )  m", sql);

    }

    
    @Test
    public void testListUseDefaultSourceQuery() {
        NsiConfigDict dict = config.getDict("dict1_view");
        NsiQuery query = dict.query().addAttr("f1");

        String sql = sqlGen.getListSql(query, null, null, -1, -1);
        Assert.assertEquals(
                "select m.f1 from ( select f1, 1 as cnt from table1 where f1 <> ? )  m", sql);

    }

}
