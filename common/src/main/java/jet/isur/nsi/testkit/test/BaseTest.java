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

import org.joda.time.DateTime;
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

    protected DictRow createTestEvent(Connection c, DictRow eventType) {

        return createTestEvent(c, eventType, null, false, null, null);
    }

    protected DictRow createTestEvent(Connection c, DictRow eventType,
            DictRow category, boolean isDeleted, Long controlLevel,
            String startDate) {
        NsiQuery query = query("EVENT");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("EVENT_TYPE_ID", eventType)
                    .attr("EVENT_CATEGORY_ID", category)
                    .attr("IS_DELETED", isDeleted)
                    .attr("CONTROL_LEVEL", controlLevel)
                    .attr("START_DATE", null == startDate ? null : BASE_DATE_FORMATTER.parseDateTime(startDate))
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestEventParam(Connection c, DictRow param,
            DictRow eventType, boolean required, DictRow block) {
        NsiQuery query = query("EVENT_PARAM");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("PARAM_ID", param)
                    .attr("EVENT_TYPE_ID", eventType)
                    .attr("IS_REQUIRED", required)
                    .attr("EVENT_PARAM_BLOCK_ID", block)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestParam(Connection c, String name, long type) {
        NsiQuery query = query("PARAM");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("PARAM_NAME", name)
                    .attr("PARAM_TYPE", type)
                    .attr("IS_GROUP", false)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestParam(Connection c, String name, long type, String dicName) {
        NsiQuery query = query("PARAM");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("PARAM_NAME", name)
                    .attr("PARAM_TYPE", type)
                    .attr("DIC_NAME", dicName)
                    .attr("IS_GROUP", false)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestEvParBlOrgRole(Connection c, DictRow block, DictRow orgRole,
            long accessMode) {
        NsiQuery query = query("EV_PAR_BL_ORG_ROLE");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("ORG_ROLE_ID", orgRole)
                    .attr("EVENT_PARAM_BLOCK_ID", block)
                    .attr("ACCESS_MODE", accessMode)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestEventParamBlock(Connection c, String name, DictRow orgRole) {
        NsiQuery query = query("EVENT_PARAM_BLOCK");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("BLOCK_NAME", name)
                    .attr("ORG_ROLE_ID",orgRole)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestOrgRole(Connection c, String name) {
        NsiQuery query = query("ORG_ROLE");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("ORG_ROLE_NAME", name)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestEventType(Connection c, String name) {
        NsiQuery query = query("EVENT_TYPE");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("EVENT_TYPE_NAME", name)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestEventCategory(Connection c, String name) {
        NsiQuery query = query("EVENT_CATEGORY");
        DictRow result = sqlDao.save(c, query,
                defaultBuilder(query).attr("EVENT_CATEGORY_KEY", name).build(),
                true);

        addTestDictRow(query, result);
        return result;
    }
    
    protected DictRow createTestMsg(Connection c,
            boolean isDeleted, DateTime sendDate, DateTime readUpDate) {
        NsiQuery query = query("MSG");
        DictRow result = sqlDao.save(c, query,
                defaultBuilder(query)
                .attr("IS_DELETED", isDeleted)
                .attr("SEND_DATE", sendDate)
                .attr("READ_UP_DATE", readUpDate)
                .build(),
                true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestMsgEmployee(Connection c,
            boolean isDeleted, DictRow msg, 
            DateTime readDate, Long orgId) {
        NsiQuery query = query("MSG_EMP");
        DictRow result = sqlDao.save(c, query,
                defaultBuilder(query)
                .attr("IS_DELETED", isDeleted)
                .attr("MSG_ID", new DictRowBuilder(query("MSG_EMP"), msg).getString("ID"))
                .attr("ORG_ID", orgId.toString())
                .attr("READ_DATE", readDate)
                .build(),
                true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestMsgInstruction(Connection c,
            boolean isDeleted, DictRow msg) {
        NsiQuery query = query("MSG_INSTRUCTION");
        DictRow result = sqlDao.save(c, query,
                defaultBuilder(query)
                .attr("IS_DELETED", isDeleted)
                .attr("MSG_ID", new DictRowBuilder(query("MSG"), msg).getString("ID"))
                .build(),
                true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestMsgInstructionOrg(Connection c,
            boolean isDeleted, DictRow msgInstruction, 
            DateTime performDate, DateTime toPerformDate, Long orgId) {
        NsiQuery query = query("MSG_INSTRUCTION_ORG");
        DictRow result = sqlDao.save(c, query,
                defaultBuilder(query)
                .attr("IS_DELETED", isDeleted)
                .attr("MSG_INSTRUCTION_ID", new DictRowBuilder(query("MSG_INSTRUCTION"), msgInstruction).getString("ID"))
                .attr("ORG_ID", orgId.toString())
                .attr("PERFORM_DATE", performDate)
                .attr("TO_PERFORM_DATE", toPerformDate)
                .build(),
                true);

        addTestDictRow(query, result);
        return result;
    }
    protected DictRow createTestEventParamValue(Connection c, DictRow event, DictRow block, DictRow param,String value) {
        NsiQuery query = query("EVENT_PARAM_VALUE");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("EVENT_ID", event)
                    .attr("EVENT_PARAM_BLOCK_ID", block)
                    .attr("PARAM_ID", param)
                    .attr("EVENT_PARAM_VALUE", value)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestShift(Connection c) {
        NsiQuery query = query("SHIFT");
        DictRow result = sqlDao.save(c, query, defaultBuilder(query).build(),
                true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestEmp(Connection c) {
        NsiQuery query = query("EMP");
        DictRow result = sqlDao.save(c, query, defaultBuilder(query).build(),
                true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestShiftROle(Connection c, String name) {
        NsiQuery query = query("SHIFT_ROLE");
        DictRow result = sqlDao.save(c, query,
                defaultBuilder(query).attr("SHIFT_ROLE_NAME", name).build(),
                true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createTestShiftEmp(Connection c, String shiftId,
            String empId, String shiftRoleId) {
        NsiQuery query = query("SHIFT_EMP");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query).attr("SHIFT_ROLE_ID", shiftRoleId)
                        .attr("SHIFT_ID", shiftId).attr("EMP_ID", empId)
                        .build(), true);

        addTestDictRow(query, result);
        return result;
    }

    protected DictRow createStaffType(Connection c, String name) {
        NsiQuery query = query("STAFF_TYPE");
        DictRow result = sqlDao.save(
                c,
                query,
                defaultBuilder(query)
                    .attr("STAFF_TYPE_NAME", name)
                    .build(), true);

        addTestDictRow(query, result);
        return result;
    }
}
