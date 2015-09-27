package jet.isur.nsi.common.sql.test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.MetaParamValue;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;
import jet.isur.nsi.testkit.test.BaseTest;
import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

public class TestSourceQuery extends BaseTest {

    protected NsiConfig config;
    protected DefaultSqlDao sqlDao;
    private DefaultSqlGen sqlGen;
    protected Map<NsiConfigDict, List<DictRow>> testDictRowMap = new HashMap<>();

    @Override
    public void setup() throws Exception {
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        configParams.setLastUserDict("USER_PROFILE");
        config = new NsiConfigManagerFactoryImpl().create(
                new File(getProperty("database.metadata.path",
                        "/opt/isur/database/metadata")), configParams)
                .getConfig();
        sqlGen = new DefaultSqlGen();
        sqlDao = new DefaultSqlDao();
        sqlDao.setSqlGen(sqlGen);
    }

    @Test
    public void testLP_EVENT_COUNT_CN_LV_sourceQuery() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            // создаем тестовые данные
            String categoryName1 = "key1";
            String categoryName2 = "key2";
            String categoryName3 = "key3";

            DictRow eventCategory1 = createTestEventCategory(c, categoryName1);
            DictRow eventCategory2 = createTestEventCategory(c, categoryName2);
            DictRow eventCategory3 = createTestEventCategory(c, categoryName3);

            Long testControlLevel = 1L;
            Long outTestControlLevel = 2L;
            
            // войдут в результат.
            createTestEvent(c, null, eventCategory1, false, testControlLevel, null);
            createTestEvent(c, null, eventCategory1, false, testControlLevel, null);
            createTestEvent(c, null, eventCategory2, false, testControlLevel, null);
            createTestEvent(c, null, eventCategory3, false, testControlLevel, null);
            createTestEvent(c, null, eventCategory3, false, testControlLevel, null);
            
            // не войдут в результат, т.к. удалены
            createTestEvent(c, null, eventCategory1, true, testControlLevel, null);
            createTestEvent(c, null, eventCategory2, true, testControlLevel, null);
            createTestEvent(c, null, eventCategory3, true, testControlLevel, null);
            // не войдут в результат -не подходит control Level
            createTestEvent(c, null, eventCategory1, false, outTestControlLevel, null);
            createTestEvent(c, null, eventCategory2, false, outTestControlLevel, null);
            createTestEvent(c, null, eventCategory3, false, outTestControlLevel, null);
            
            NsiQuery query = query("LP_EVENT_COUNT_CN_LV").addAttrs();
            List<MetaParamValue> params = new ArrayList<MetaParamValue>();
            params.add(new MetaParamValue(MetaFieldType.NUMBER, testControlLevel.toString()));
            List<DictRow> rows = sqlDao.list(c, query, null, null, -1, -1, "MAIN", params);
            Assert.assertEquals(3, rows.size());
            for (DictRow cur : rows) {
                DictRowBuilder builder = new DictRowBuilder(query, cur);
                String categoryName = builder.getString("EVENT_CATEGORY_KEY");
                Long count = builder.getLong("EVENT_CNT");
                if (categoryName1.equals(categoryName))
                    Assert.assertEquals(2, count.intValue());
                else if (categoryName2.equals(categoryName))
                    Assert.assertEquals(1, count.intValue());
                else if (categoryName3.equals(categoryName))
                    Assert.assertEquals(2, count.intValue());
            }
        }
    }

    @Test
    public void testLP_EVENT_CNTsourceQuery() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            // создаем тестовые данные
            String categoryName1 = "key1";
            String categoryName2 = "key2";
            String categoryName3 = "key3";

            DateTime testDateFrom = BASE_DATE_FORMATTER.parseDateTime("12.09.2015");
            DateTime testDateTo = BASE_DATE_FORMATTER.parseDateTime("20.09.2015");
                    
            DictRow eventCategory1 = createTestEventCategory(c, categoryName1);
            DictRow eventCategory2 = createTestEventCategory(c, categoryName2);
            DictRow eventCategory3 = createTestEventCategory(c, categoryName3);

            Long testControlLevel = 1L;
            Long outTestControlLevel = 2L;

            // войдут в результат.
            createTestEvent(c, null, eventCategory1, false, testControlLevel, "14.09.2015");
            createTestEvent(c, null, eventCategory1, false, testControlLevel, "12.09.2015");
            createTestEvent(c, null, eventCategory2, false, testControlLevel, "15.09.2015");
            createTestEvent(c, null, eventCategory3, false, testControlLevel, "16.09.2015");
            createTestEvent(c, null, eventCategory3, false, testControlLevel, "19.09.2015");

            // не войдут в результат, т.к. удалены
            createTestEvent(c, null, eventCategory1, true, testControlLevel, "14.09.2015");
            createTestEvent(c, null, eventCategory2, true, testControlLevel, "15.09.2015");
            createTestEvent(c, null, eventCategory3, true, testControlLevel, "16.09.2015");

            // не войдут в результат - не попадают в диапазон
            createTestEvent(c, null, eventCategory1, false, outTestControlLevel, "10.09.2015");
            createTestEvent(c, null, eventCategory2, false, outTestControlLevel, "09.09.2015");
            createTestEvent(c, null, eventCategory3, false, outTestControlLevel, "08.09.2015");
            createTestEvent(c, null, eventCategory1, false, outTestControlLevel, "21.09.2015");
            createTestEvent(c, null, eventCategory2, false, outTestControlLevel, "22.09.2015");
            createTestEvent(c, null, eventCategory3, false, outTestControlLevel, "23.09.2015");

            NsiQuery query = query("LP_EVENT_CNT").addAttrs();
            List<MetaParamValue> params = new ArrayList<MetaParamValue>();
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, testDateFrom.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, testDateTo.toString()));

            List<DictRow> rows = sqlDao.list(c, query, null, null, -1, -1, "MAIN", params);
            Assert.assertEquals(3, rows.size());
            for (DictRow cur : rows) {
                DictRowBuilder builder = new DictRowBuilder(query, cur);
                String categoryName = builder.getString("EVENT_CATEGORY_KEY");
                Long count = builder.getLong("EVENT_CNT");
                if (categoryName1.equals(categoryName))
                    Assert.assertEquals(2, count.intValue());
                else if (categoryName2.equals(categoryName))
                    Assert.assertEquals(1, count.intValue());
                else if (categoryName3.equals(categoryName))
                    Assert.assertEquals(2, count.intValue());
            }
        }
    }

    @Test
    // получения информации о сообщениях сотрудникам с разбивкой по обществам
    public void testLP_MSG_CMP_CNT_ORGsourceQuery() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            DateTime base = new DateTime();
            Long testOrg1 = 1L;
            Long testOrg2 = 2L;
            // to Прочитано
            DictRow msg1  = createTestMsg(c, false,  base.minusDays(3),  base);
            DictRow msg2  = createTestMsg(c, false,  base.minusDays(2),  base.plusDays(1));
            DictRow msg3  = createTestMsg(c, false,  base.minusDays(4),  base.plusDays(2));
            DictRow msg4  = createTestMsg(c, false,  base.minusDays(5),  base);
            // to Прочитано и просрочено
            DictRow msg5  = createTestMsg(c, false,  base.minusDays(3),  base.minusDays(1)); 
            DictRow msg6  = createTestMsg(c, false,  base.minusDays(2),  base.minusDays(2)); 
            DictRow msg7  = createTestMsg(c, false,  base.minusDays(5),  base.minusDays(3));
            DictRow msg8  = createTestMsg(c, false,  base.minusDays(4),  base.minusDays(1));
            // to Не прочитано
            DictRow msg9   = createTestMsg(c, false,  base.minusDays(3),  base.plusDays(1));
            DictRow msg10  = createTestMsg(c, false,  base.minusDays(2),  base.plusDays(2));
            DictRow msg11  = createTestMsg(c, false,  base.minusDays(5),  base.plusDays(2));
            DictRow msg12  = createTestMsg(c, false,  base.minusDays(3),  base.plusDays(4));
            
            // нет соотв. записей в MSG_EMP
            createTestMsg(c, false, base.minusDays(4), null);
            createTestMsg(c, false, base.minusDays(3), null);
            // не попадают по датам
            DictRow msg15  = createTestMsg(c, false,  base.minusDays(10), null);
            DictRow msg16  = createTestMsg(c, false,  base.minusDays(10), null);
            // IS_DELETED = 'Y'
            DictRow msg17  = createTestMsg(c, true,   base.minusDays(1),  null);
            DictRow msg18  = createTestMsg(c, true,   base,               null);
            // MSG_EMP IS_DELETED = 'Y'
            DictRow msg19  = createTestMsg(c, false,  base.minusDays(2),  null);
            DictRow msg20  = createTestMsg(c, false,  base.minusDays(1),  null);
            // to Не прочитано и просрочено
            DictRow msg21  = createTestMsg(c, false,  base.minusDays(4),  base.minusDays(2));
            DictRow msg22  = createTestMsg(c, false,  base.minusDays(5),  base.minusDays(3));
            DictRow msg23  = createTestMsg(c, false,  base.minusDays(3),  base.minusDays(1));
            DictRow msg24  = createTestMsg(c, false,  base.minusDays(4),  base.minusDays(4));
             
            // MSG_EMP
            createTestMsgEmployee(c, false, msg1, base.minusDays(2), testOrg2);
            createTestMsgEmployee(c, false, msg2, base,              testOrg2);
            createTestMsgEmployee(c, false, msg3, base,              testOrg1);
            createTestMsgEmployee(c, false, msg4, base.minusDays(2), testOrg2);
            
            createTestMsgEmployee(c, false, msg5,  base,              testOrg2);
            createTestMsgEmployee(c, false, msg6,  base.minusDays(1), testOrg1);
            createTestMsgEmployee(c, false, msg7,  base.minusDays(2), testOrg1);
            createTestMsgEmployee(c, false, msg8,  base,              testOrg2);
            
            createTestMsgEmployee(c, false, msg9,  null,              testOrg1);
            createTestMsgEmployee(c, false, msg10, null,              testOrg1);
            createTestMsgEmployee(c, false, msg11, null,              testOrg1);
            createTestMsgEmployee(c, false, msg12, null,              testOrg2);
            
            createTestMsgEmployee(c, false, msg21, null,              testOrg1);
            createTestMsgEmployee(c, false, msg22, null,              testOrg1);
            createTestMsgEmployee(c, false, msg23, null,              testOrg1);
            createTestMsgEmployee(c, false, msg24, null,              testOrg1);

            createTestMsgEmployee(c, false, msg15, null,              testOrg1);
            createTestMsgEmployee(c, false, msg16, null,              testOrg2);
            createTestMsgEmployee(c, false, msg17, null,              testOrg1);
            createTestMsgEmployee(c, false, msg18, null,              testOrg2);
            createTestMsgEmployee(c, true,  msg19, null,              testOrg1);
            createTestMsgEmployee(c, true,  msg20, base.minusDays(2), testOrg2);

            
            NsiQuery query = query("LP_MSG_EMP_CNT_ORG").addAttrs();
            List<MetaParamValue> params = new ArrayList<MetaParamValue>();
            DateTime paramDate = base.minusDays(6);
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            
            List<DictRow> rows = sqlDao.list(c, query, null, null, -1, -1, "MAIN", params);
            for (DictRow cur : rows) {
                DictRowBuilder builder = new DictRowBuilder(query, cur);
                Long orgId = builder.getLong("ORG_ID");
                if (testOrg1.equals(orgId)){
                    Assert.assertEquals(10, builder.getLong("TOTAL_CNT").intValue());
                    Assert.assertEquals(1, builder.getLong("READ_CNT").intValue());
                    Assert.assertEquals(2, builder.getLong("READ_OVER_CNT").intValue());
                    Assert.assertEquals(3, builder.getLong("NOT_READ_CNT").intValue());
                    Assert.assertEquals(4, builder.getLong("NOT_READ_OVER_CNT").intValue());
                } else if (testOrg2.equals(orgId)) {
                    Assert.assertEquals(6, builder.getLong("TOTAL_CNT").intValue());
                    Assert.assertEquals(3, builder.getLong("READ_CNT").intValue());
                    Assert.assertEquals(2, builder.getLong("READ_OVER_CNT").intValue());
                    Assert.assertEquals(1, builder.getLong("NOT_READ_CNT").intValue());
                    Assert.assertEquals(null, builder.getLong("NOT_READ_OVER_CNT"));
                }
            }
        }
    }
    
    @Test
    // получения информации о сообщениях сотрудникам
    public void testLP_MSG_EMP_CNTsourceQuery() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            DateTime base = new DateTime();
            Long testOrg1 = 1L;
            Long testOrg2 = 2L;
            // to Прочитано
            DictRow msg1  = createTestMsg(c, false,  base.minusDays(3),  base);
            DictRow msg2  = createTestMsg(c, false,  base.minusDays(2),  base.plusDays(1));
            DictRow msg3  = createTestMsg(c, false,  base.minusDays(4),  base.plusDays(2));
            DictRow msg4  = createTestMsg(c, false,  base.minusDays(5),  base);
            // to Прочитано и просрочено
            DictRow msg5  = createTestMsg(c, false,  base.minusDays(3),  base.minusDays(1)); 
            DictRow msg6  = createTestMsg(c, false,  base.minusDays(2),  base.minusDays(2)); 
            DictRow msg7  = createTestMsg(c, false,  base.minusDays(5),  base.minusDays(3));
            // to Не прочитано
            DictRow msg9   = createTestMsg(c, false,  base.minusDays(3),  base.plusDays(1));
            DictRow msg10  = createTestMsg(c, false,  base.minusDays(2),  base.plusDays(2));
            
            // нет соотв. записей в MSG_EMP
            createTestMsg(c, false, base.minusDays(4), null);
            createTestMsg(c, false, base.minusDays(3), null);
            // не попадают по датам
            DictRow msg15  = createTestMsg(c, false,  base.minusDays(10), null);
            DictRow msg16  = createTestMsg(c, false,  base.minusDays(10), null);
            // IS_DELETED = 'Y'
            DictRow msg17  = createTestMsg(c, true,   base.minusDays(1),  null);
            DictRow msg18  = createTestMsg(c, true,   base,               null);
            // MSG_EMP IS_DELETED = 'Y'
            DictRow msg19  = createTestMsg(c, false,  base.minusDays(2),  null);
            DictRow msg20  = createTestMsg(c, false,  base.minusDays(1),  null);
            // to Не прочитано и просрочено
            DictRow msg22  = createTestMsg(c, false,  base.minusDays(5),  base.minusDays(3));

            // MSG_EMP
            createTestMsgEmployee(c, false, msg1, base.minusDays(2), testOrg2);
            createTestMsgEmployee(c, false, msg2, base,              testOrg2);
            createTestMsgEmployee(c, false, msg3, base,              testOrg1);
            createTestMsgEmployee(c, false, msg4, base.minusDays(2), testOrg2);
            
            createTestMsgEmployee(c, false, msg5,  base,              testOrg2);
            createTestMsgEmployee(c, false, msg6,  base.minusDays(1), testOrg1);
            createTestMsgEmployee(c, false, msg7,  base.minusDays(2), testOrg1);
            
            createTestMsgEmployee(c, false, msg9,  null,              testOrg1);
            createTestMsgEmployee(c, false, msg10, null,              testOrg1);
            
            createTestMsgEmployee(c, false, msg22, null,              testOrg1);

            createTestMsgEmployee(c, false, msg15, null,              testOrg1);
            createTestMsgEmployee(c, false, msg16, null,              testOrg2);
            createTestMsgEmployee(c, false, msg17, null,              testOrg1);
            createTestMsgEmployee(c, false, msg18, null,              testOrg2);
            createTestMsgEmployee(c, true,  msg19, null,              testOrg1);
            createTestMsgEmployee(c, true,  msg20, base.minusDays(2), testOrg2);
            
            NsiQuery query = query("LP_MSG_EMP_CNT").addAttrs();
            List<MetaParamValue> params = new ArrayList<MetaParamValue>();
            DateTime paramDate = base.minusDays(6);
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            
            List<DictRow> rows = sqlDao.list(c, query, null, null, -1, -1, "MAIN", params);
            Assert.assertEquals(1, rows.size());
            for (DictRow cur : rows) {
                DictRowBuilder builder = new DictRowBuilder(query, cur);
                Assert.assertEquals(10, builder.getLong("TOTAL_CNT").intValue());
                Assert.assertEquals(4, builder.getLong("READ_CNT").intValue());
                Assert.assertEquals(3, builder.getLong("READ_OVER_CNT").intValue());
                Assert.assertEquals(2, builder.getLong("NOT_READ_CNT").intValue());
                Assert.assertEquals(1, builder.getLong("NOT_READ_OVER_CNT").intValue());
            }
        }
    }

    @Test
    // получения информации о сообщениях сотрудникам
    public void testLP_MSG_INSTRUCTION_ORG_CNT_ORGsourceQuery() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            DateTime base = new DateTime();
            Long testOrg1 = 1L;
            Long testOrg2 = 2L;
            
            // to Исполнено
            DictRow msg1   = createTestMsg(c, false,  base.minusDays(3), null);
            DictRow msg2   = createTestMsg(c, false,  base.minusDays(2), null);
            DictRow msg3   = createTestMsg(c, false,  base.minusDays(4), null);
            DictRow msg4   = createTestMsg(c, false,  base.minusDays(4), null);
            // to Исполнено и просрочено
            DictRow msg5   = createTestMsg(c, false,  base.minusDays(4), null);
            DictRow msg6   = createTestMsg(c, false,  base.minusDays(3), null);
            DictRow msg7   = createTestMsg(c, false,  base.minusDays(2), null);
            DictRow msg8   = createTestMsg(c, false,  base.minusDays(4), null);
            // to Не исполнено
            DictRow msg9   = createTestMsg(c, false,  base.minusDays(4), null);
            DictRow msg10  = createTestMsg(c, false,  base.minusDays(3), null);
            DictRow msg11  = createTestMsg(c, false,  base.minusDays(2), null);
            DictRow msg12  = createTestMsg(c, false,  base.minusDays(1), null);
            //не попадают в результат - нет в MSG_INSTRUCTION
            createTestMsg(c, false, base.minusDays(4), null);
            createTestMsg(c, false, base.minusDays(3), null);
            //не попадают в результат - не проходят по датам            
            DictRow msg15  = createTestMsg(c, false,  base.minusDays(10),null);
            DictRow msg16  = createTestMsg(c, false,  base.minusDays(10),null);
            //не попадают в результат - IS_DELETED = 'Y'
            DictRow msg17  = createTestMsg(c, true,   base.minusDays(1), null);
            DictRow msg18  = createTestMsg(c, true,   base,              null);
            //не попадают в результат - MSG_INSTRUCTION_ORG IS_DELETED = 'Y'
            DictRow msg19  = createTestMsg(c, false,  base.minusDays(2), null);
            DictRow msg20  = createTestMsg(c, false,  base.minusDays(1), null);
            //to Не исполнено и просрочено
            DictRow msg21  = createTestMsg(c, false,  base.minusDays(3), null);
            DictRow msg22  = createTestMsg(c, false,  base.minusDays(4), null);
            DictRow msg23  = createTestMsg(c, false,  base.minusDays(1), null);
            DictRow msg24  = createTestMsg(c, false,  base.minusDays(3), null);
            
            // to Исполнено
            DictRow msgInstr1   = createTestMsgInstruction(c, false, msg1);
            DictRow msgInstr2   = createTestMsgInstruction(c, false, msg2);
            DictRow msgInstr3   = createTestMsgInstruction(c, false, msg3);
            DictRow msgInstr4   = createTestMsgInstruction(c, false, msg4);
            // to Исполнено и просрочено
            DictRow msgInstr5   = createTestMsgInstruction(c, false, msg5);
            DictRow msgInstr6   = createTestMsgInstruction(c, false, msg6);
            DictRow msgInstr7   = createTestMsgInstruction(c, false, msg7);
            DictRow msgInstr8   = createTestMsgInstruction(c, false, msg8);
            // to Не исполнено
            DictRow msgInstr9   = createTestMsgInstruction(c, false, msg9);
            DictRow msgInstr10  = createTestMsgInstruction(c, false, msg10);
            DictRow msgInstr11  = createTestMsgInstruction(c, false, msg11);
            DictRow msgInstr12  = createTestMsgInstruction(c, false, msg12);
            //не попадают в результат - нет в MSG_INSTRUCTION_ORG
            createTestMsgInstruction(c, false, null);
            createTestMsgInstruction(c, false, null);
            //не попадают в результат - не проходят по датам            
            DictRow msgInstr15  = createTestMsgInstruction(c, false, msg15);
            DictRow msgInstr16  = createTestMsgInstruction(c, false, msg16);
            //не попадают в результат - IS_DELETED = 'Y'
            createTestMsgInstruction(c, true, msg17);
            createTestMsgInstruction(c, true, msg18);
            //не попадают в результат - MSG_INSTRUCTION_ORG IS_DELETED = 'Y'
            DictRow msgInstr19  = createTestMsgInstruction(c, false, msg19);
            DictRow msgInstr20  = createTestMsgInstruction(c, false, msg20);
            //to Не исполнено и просрочено
            DictRow msgInstr21  = createTestMsgInstruction(c, false, msg21);
            DictRow msgInstr22  = createTestMsgInstruction(c, false, msg22);
            DictRow msgInstr23  = createTestMsgInstruction(c, false, msg23);
            DictRow msgInstr24  = createTestMsgInstruction(c, false, msg24);
            
            createTestMsgInstructionOrg(c, false, msgInstr1,  base.minusDays(2), base.minusDays(1), testOrg2);
            createTestMsgInstructionOrg(c, false, msgInstr2,  base.minusDays(5), base.minusDays(5), testOrg2);
            createTestMsgInstructionOrg(c, false, msgInstr3,  base.minusDays(4), base.minusDays(2), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr4,  base.minusDays(3), base,              testOrg2);
            
            createTestMsgInstructionOrg(c, false, msgInstr5,  base,              base.minusDays(3), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr6,  base.minusDays(1), base.minusDays(2), testOrg2);
            createTestMsgInstructionOrg(c, false, msgInstr7,  base.minusDays(2), base.minusDays(5), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr8,  base,              base.minusDays(1), testOrg2);
            
            createTestMsgInstructionOrg(c, false, msgInstr9,  null,              base.plusDays(2),  testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr10, null,              base.plusDays(2),  testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr11, null,              base.plusDays(3),  testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr12, null,              base.plusDays(1),  testOrg2);
            
            createTestMsgInstructionOrg(c, false, msgInstr15, base.minusDays(3), base.minusDays(3), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr16, base.minusDays(3), base.minusDays(3), testOrg2);
            createTestMsgInstructionOrg(c, true,  msgInstr19, base.minusDays(3), base.minusDays(3), testOrg1);
            createTestMsgInstructionOrg(c, true,  msgInstr20, base.minusDays(3), base.minusDays(3), testOrg2);
            
            createTestMsgInstructionOrg(c, false, msgInstr21, null,              base.minusDays(1), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr22, null,              base.minusDays(2), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr23, null,              base.minusDays(1), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr24, null,              base.minusDays(3), testOrg1);
            
            NsiQuery query = query("LP_MSG_INSTRUCTION_ORG_CNT_ORG").addAttrs();
            List<MetaParamValue> params = new ArrayList<MetaParamValue>();
            DateTime paramDate = base.minusDays(5);
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            
            List<DictRow> rows = sqlDao.list(c, query, null, null, -1, -1, "MAIN", params);
            for (DictRow cur : rows) {
                DictRowBuilder builder = new DictRowBuilder(query, cur);
                Long orgId = builder.getLong("ORG_ID");
                if (testOrg1.equals(orgId)){
                    Assert.assertEquals(10, builder.getLong("TOTAL_CNT").intValue());
                    Assert.assertEquals(1, builder.getLong("EXEC_CNT").intValue());
                    Assert.assertEquals(2, builder.getLong("EXEC_OVER_CNT").intValue());
                    Assert.assertEquals(3, builder.getLong("NOT_EXEC_CNT").intValue());
                    Assert.assertEquals(4, builder.getLong("NOT_EXEC_OVER_CNT").intValue());
                } else if (testOrg2.equals(orgId)) {
                    Assert.assertEquals(6, builder.getLong("TOTAL_CNT").intValue());
                    Assert.assertEquals(3, builder.getLong("EXEC_CNT").intValue());
                    Assert.assertEquals(2, builder.getLong("EXEC_OVER_CNT").intValue());
                    Assert.assertEquals(1, builder.getLong("NOT_EXEC_CNT").intValue());
                    Assert.assertEquals(null, builder.getLong("NOT_EXEC_OVER_CNT"));
                }
            }
        }
    }

    @Test
    // получения информации о сообщениях сотрудникам
    public void testLP_MSG_INSTRUCTION_ORG_CNTsourceQuery() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            DateTime base = new DateTime();
            Long testOrg1 = 1L;
            Long testOrg2 = 2L;
            
            // to Исполнено
            DictRow msg1   = createTestMsg(c, false,  base.minusDays(3), null);
            DictRow msg2   = createTestMsg(c, false,  base.minusDays(2), null);
            DictRow msg3   = createTestMsg(c, false,  base.minusDays(4), null);
            DictRow msg4   = createTestMsg(c, false,  base.minusDays(4), null);
            // to Исполнено и просрочено
            DictRow msg5   = createTestMsg(c, false,  base.minusDays(4), null);
            DictRow msg6   = createTestMsg(c, false,  base.minusDays(3), null);
            DictRow msg7   = createTestMsg(c, false,  base.minusDays(2), null);
            // to Не исполнено
            DictRow msg9   = createTestMsg(c, false,  base.minusDays(4), null);
            DictRow msg10  = createTestMsg(c, false,  base.minusDays(3), null);
            //не попадают в результат - нет в MSG_INSTRUCTION
            createTestMsg(c, false, base.minusDays(4), null);
            createTestMsg(c, false, base.minusDays(3), null);
            //не попадают в результат - не проходят по датам            
            DictRow msg15  = createTestMsg(c, false,  base.minusDays(10),null);
            DictRow msg16  = createTestMsg(c, false,  base.minusDays(10),null);
            //не попадают в результат - IS_DELETED = 'Y'
            DictRow msg17  = createTestMsg(c, true,   base.minusDays(1), null);
            DictRow msg18  = createTestMsg(c, true,   base,              null);
            //не попадают в результат - MSG_INSTRUCTION_ORG IS_DELETED = 'Y'
            DictRow msg19  = createTestMsg(c, false,  base.minusDays(2), null);
            DictRow msg20  = createTestMsg(c, false,  base.minusDays(1), null);
            //to Не исполнено и просрочено
            DictRow msg22  = createTestMsg(c, false,  base.minusDays(4), null);
            
            // to Исполнено
            DictRow msgInstr1   = createTestMsgInstruction(c, false, msg1);
            DictRow msgInstr2   = createTestMsgInstruction(c, false, msg2);
            DictRow msgInstr3   = createTestMsgInstruction(c, false, msg3);
            DictRow msgInstr4   = createTestMsgInstruction(c, false, msg4);
            // to Исполнено и просрочено
            DictRow msgInstr5   = createTestMsgInstruction(c, false, msg5);
            DictRow msgInstr6   = createTestMsgInstruction(c, false, msg6);
            DictRow msgInstr7   = createTestMsgInstruction(c, false, msg7);
            // to Не исполнено
            DictRow msgInstr9   = createTestMsgInstruction(c, false, msg9);
            DictRow msgInstr10  = createTestMsgInstruction(c, false, msg10);
            //не попадают в результат - нет в MSG_INSTRUCTION_ORG
            createTestMsgInstruction(c, false, null);
            createTestMsgInstruction(c, false, null);
            //не попадают в результат - не проходят по датам            
            DictRow msgInstr15  = createTestMsgInstruction(c, false, msg15);
            DictRow msgInstr16  = createTestMsgInstruction(c, false, msg16);
            //не попадают в результат - IS_DELETED = 'Y'
            createTestMsgInstruction(c, true, msg17);
            createTestMsgInstruction(c, true, msg18);
            //не попадают в результат - MSG_INSTRUCTION_ORG IS_DELETED = 'Y'
            DictRow msgInstr19  = createTestMsgInstruction(c, false, msg19);
            DictRow msgInstr20  = createTestMsgInstruction(c, false, msg20);
            //to Не исполнено и просрочено
            DictRow msgInstr22  = createTestMsgInstruction(c, false, msg22);
            
            createTestMsgInstructionOrg(c, false, msgInstr1,  base.minusDays(2), base.minusDays(1), testOrg2);
            createTestMsgInstructionOrg(c, false, msgInstr2,  base.minusDays(5), base.minusDays(5), testOrg2);
            createTestMsgInstructionOrg(c, false, msgInstr3,  base.minusDays(4), base.minusDays(2), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr4,  base.minusDays(3), base,              testOrg2);
            
            createTestMsgInstructionOrg(c, false, msgInstr5,  base,              base.minusDays(3), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr6,  base.minusDays(1), base.minusDays(2), testOrg2);
            createTestMsgInstructionOrg(c, false, msgInstr7,  base.minusDays(2), base.minusDays(5), testOrg1);
            
            createTestMsgInstructionOrg(c, false, msgInstr9,  null,              base.plusDays(2),  testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr10, null,              base.plusDays(2),  testOrg1);
            
            createTestMsgInstructionOrg(c, false, msgInstr15, base.minusDays(3), base.minusDays(3), testOrg1);
            createTestMsgInstructionOrg(c, false, msgInstr16, base.minusDays(3), base.minusDays(3), testOrg2);
            createTestMsgInstructionOrg(c, true,  msgInstr19, base.minusDays(3), base.minusDays(3), testOrg1);
            createTestMsgInstructionOrg(c, true,  msgInstr20, base.minusDays(3), base.minusDays(3), testOrg2);
            
            createTestMsgInstructionOrg(c, false, msgInstr22, null,              base.minusDays(2), testOrg1);
            
            NsiQuery query = query("LP_MSG_INSTRUCTION_ORG_CNT").addAttrs();
            List<MetaParamValue> params = new ArrayList<MetaParamValue>();
            DateTime paramDate = base.minusDays(5);
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            params.add(new MetaParamValue(MetaFieldType.DATE_TIME, paramDate.toString()));
            
            List<DictRow> rows = sqlDao.list(c, query, null, null, -1, -1, "MAIN", params);
            Assert.assertEquals(1, rows.size());
            for (DictRow cur : rows) {
                DictRowBuilder builder = new DictRowBuilder(query, cur);
                Assert.assertEquals(10, builder.getLong("TOTAL_CNT").intValue());
                Assert.assertEquals(4, builder.getLong("EXEC_CNT").intValue());
                Assert.assertEquals(3, builder.getLong("EXEC_OVER_CNT").intValue());
                Assert.assertEquals(2, builder.getLong("NOT_EXEC_CNT").intValue());
                Assert.assertEquals(1, builder.getLong("NOT_EXEC_OVER_CNT").intValue());
            }
        }
    }



}
