package jet.isur.nsi.services.sql.test;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.NsiError;
import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;
import jet.isur.nsi.services.NsiGenericServiceImpl;
import jet.isur.nsi.services.NsiTransactionServiceImpl;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import jet.isur.nsi.testkit.utils.DaoUtils;
import jet.scdp.metrics.mock.MockMetrics;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class TestNsiGenericServiceImpl extends BaseSqlTest {

    private DefaultSqlDao sqlDao;
    private DefaultSqlGen sqlGen;
    private NsiConfig config;
    private NsiGenericServiceImpl service;
    private NsiTransactionServiceImpl transactionService;

    @Override
    public void setup() throws Exception {
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("src/test/resources/metadata1"), configParams ).getConfig();
        sqlGen = new DefaultSqlGen();
        sqlDao = new DefaultSqlDao();
        sqlDao.setSqlGen(sqlGen);
        transactionService = new NsiTransactionServiceImpl(new MockMetrics());
        transactionService.setDataSource(dataSource);
        service = new NsiGenericServiceImpl(new MockMetrics());
        service.setTransactionService(transactionService);
    }

    @Test
    public void testInsertVeryLongValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    
                    try{
                       service.dictSave("", createData(query, "01234567890"), sqlDao);
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                    
                    try{
                        service.dictSave("", createData(query, 
                                "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
                                ), sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.MAX_FIELD_LENGTH_EXCEEDED, e.getError());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
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
    public void testInsertVeryLongValueOnBatchSave() throws Exception {
        NsiConfigDict dict = config.getDict("dict1");
        try (Connection connection = dataSource.getConnection()) {
            DaoUtils.recreateTable(dict, connection);
            try {
                DaoUtils.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    List<DictRow> data = new ArrayList<>();
                    data.add(createData(query, "01234567890"));
                    data.add(createData(query, "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"));
                    
                    try{
                        service.dictBatchSave("", data, sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.MAX_FIELD_LENGTH_EXCEEDED, e.getError());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                    
                } finally {
                    DaoUtils.dropSeq(dict, connection);
                }

            } finally {
                DaoUtils.dropTable(dict, connection);
            }
        }
    }

    private DictRow createData(NsiQuery query, String value) {
        DictRow inData = query.getDict().builder()
                .deleteMarkAttr(false)
                .idAttrNull()
                .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                .lastUserAttr(null)
                .attr("f1", value)
                .attr("ORG_ID", 1L)
                .attr("ORG_ROLE_ID", 2L)
                .build();
        return inData;
    }
   
}
