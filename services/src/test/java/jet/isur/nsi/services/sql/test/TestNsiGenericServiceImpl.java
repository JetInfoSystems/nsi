package jet.isur.nsi.services.sql.test;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import jet.isur.nsi.api.NsiError;
import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.services.NsiGenericServiceImpl;
import jet.isur.nsi.services.NsiTransactionServiceImpl;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import jet.scdp.metrics.mock.MockMetrics;

public class TestNsiGenericServiceImpl extends BaseSqlTest {

    private NsiConfig config;
    private NsiGenericServiceImpl service;
    private NsiTransactionServiceImpl transactionService;

    @Override
    public void setup() throws Exception {
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("src/test/resources/metadata1"), configParams ).getConfig();
        transactionService = new NsiTransactionServiceImpl(new MockMetrics());
        transactionService.setDataSource(dataSource);
        service = new NsiGenericServiceImpl(new MockMetrics());
        service.setTransactionService(transactionService);
    }

    @Test
    public void testInsertIncorrectVarcharValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();

                    try{
                        service.dictSave("", createData(query, "varchar_field", "0123456789"), sqlDao);
                    }catch(Exception e){
                        Assert.fail(e.getMessage());
                    }

                    try{
                        service.dictSave("", createData(query, "varchar_field", "012345678952"), sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals("Атрибут 'varchar_field' не прошел валидацию - длина строки [12] больше максимальной [10]", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertIncorrectCharValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();

                    try{
                        service.dictSave("", createData(query, "char_field", "2"), sqlDao);
                        service.dictSave("", createData(query, "char_field", "dd"), sqlDao);
                    }catch(Exception e){
                        Assert.fail(e.getMessage());
                    }

                    try{
                        service.dictSave("", createData(query, "char_field", "dddd"), sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals("Атрибут 'char_field' не прошел валидацию - длина строки [4] больше максимальной [2]", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertIncorrectBooleanValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();

                    try{
                        service.dictSave("", createData(query, "boolean_field", "true"), sqlDao);
                        service.dictSave("", createData(query, "boolean_field", "false"), sqlDao);
                    }catch(Exception e){
                        Assert.fail(e.getMessage());
                    }

                    try{
                        service.dictSave("", createData(query, "boolean_field", "dd"), sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals( "Атрибут 'boolean_field' не прошел валидацию - значение 'dd' не является логическим", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertIncorrectDateTimeValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    try{
                        service.dictSave("", createData(query, "datetime_field", "2015-11-13T08:55:22.861+02:00"), sqlDao);
                    }catch(Exception e){
                        Assert.fail(e.getMessage());
                    }

                    try{
                        service.dictSave("", createData(query, "datetime_field", "someDate"), sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals( "Атрибут 'datetime_field' не прошел валидацию - значение [someDate] нельзя преобразовать в дату", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertIncorrectEnumValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    try{
                        service.dictSave("", createData(query, "enum_field", "A"), sqlDao);
                        service.dictSave("", createData(query, "enum_field", "B"), sqlDao);
                        service.dictSave("", createData(query, "enum_field", "C"), sqlDao);
                    }catch(Exception e){
                        Assert.fail(e.getMessage());
                    }

                    try{
                        service.dictSave("", createData(query, "enum_field", "D"), sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals( "Атрибут 'enum_field' не прошел валидацию - значение [D] не является допустимым значением перечисления", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertIncorrectNumberIntValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    try{
                        service.dictSave("", createData(query, "number_field_int", "1254"), sqlDao);
                        service.dictSave("", createData(query, "number_field_int", "-154"), sqlDao);
                    }catch(Exception e){
                        Assert.fail(e.getMessage());
                    }
//                    - {name: number_field_int, size: 5, type: number}
                    String msgPtrn = "Атрибут 'number_field_int' не прошел валидацию - значение [%s] не является целым числом с максимальной длинной [5]";
                    checkNumber(query, "number_field_int", "someDate", msgPtrn);
                    checkNumber(query, "number_field_int", "123.55", msgPtrn);
                    checkNumber(query, "number_field_int", "-45.24", msgPtrn);
                    checkNumber(query, "number_field_int", "4nf", msgPtrn);
                    checkNumber(query, "number_field_int", "4125489", msgPtrn);
                    checkNumber(query, "number_field_int", "-412514", msgPtrn);
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testInsertIncorrectNumberDoubleValue() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    try{
                        service.dictSave("", createData(query, "number_field_double", "125.68"), sqlDao);
                        service.dictSave("", createData(query, "number_field_double", "-154.994587"), sqlDao);
                        service.dictSave("", createData(query, "number_field_double", "-12345678.25"), sqlDao);
                    }catch(Exception e){
                        Assert.fail(e.getMessage());
                    }
//                    - {name: number_field_double, size: 12, precision: 3, type: number}
                    String msgPtrn = "Атрибут 'number_field_double' не прошел валидацию - значение [%s] не является числом с максимальной длинной целой части [9]";
                    checkNumber(query, "number_field_double", "someDate", msgPtrn);
                    checkNumber(query, "number_field_double", "1234567891.55", msgPtrn);
                    checkNumber(query, "number_field_double", "-1254214745.12", msgPtrn);
                    checkNumber(query, "number_field_double", "1d4v5f4", msgPtrn);
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    private void checkNumber(NsiQuery query, String fieldName, String value, String msgPtrn) {
        try{
            service.dictSave("", createData(query, fieldName, value), sqlDao);
            Assert.fail("exception must be thrown");
        }catch (NsiServiceException e){
            Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
            Assert.assertEquals(String.format(msgPtrn, value),  e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            Assert.fail("incorrect exception throwed");
        }
    }
    
    @Test
    public void testInsertDatetimeValueOnBatchSave() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    List<DictRow> data = new ArrayList<>();
                    data.add(createData(query, "datetime_field", "2015-11-13T08:55:22.861+02:00"));
                    data.add(createData(query, "datetime_field", "2015-01-14T08:55:22.861+02:00"));
                    data.add(createData(query, "datetime_field", "testDate"));
                    try{
                        service.dictBatchSave("", data, sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals( "Атрибут 'datetime_field' не прошел валидацию - значение [testDate] нельзя преобразовать в дату", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                    
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertVarcharValueOnBatchSave() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    List<DictRow> data = new ArrayList<>();
                    data.add(createData(query, "varchar_field", "0234567890"));
                    data.add(createData(query, "varchar_field", "012345678911"));
                    
                    try{
                        service.dictBatchSave("", data, sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals("Атрибут 'varchar_field' не прошел валидацию - длина строки [12] больше максимальной [10]", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                    
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }

    @Test
    public void testInsertIncorrectCharValueOnBatchSave() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    List<DictRow> data = new ArrayList<>();
                    data.add(createData(query, "char_field", "d"));
                    data.add(createData(query, "char_field", "22"));
                    data.add(createData(query, "char_field", "fg4gf"));
                    
                    try{
                        service.dictBatchSave("", data, sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals("Атрибут 'char_field' не прошел валидацию - длина строки [5] больше максимальной [2]", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                    
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    @Test
    public void testInsertIncorrectBooleanValueOnBatchSave() throws Exception {
        NsiConfigDict dict = config.getDict("dict_ext");
        try (Connection connection = dataSource.getConnection()) {
            platformSqlDao.recreateTable(dict, connection);
            try {
                platformSqlDao.recreateSeq(dict, connection);
                try {
                    NsiQuery query = dict.query().addAttrs();
                    List<DictRow> data = new ArrayList<>();
                    data.add(createData(query, "boolean_field", "false"));
                    data.add(createData(query, "boolean_field", "true"));
                    data.add(createData(query, "boolean_field", "test"));
                    
                    try{
                        service.dictBatchSave("", data, sqlDao);
                        Assert.fail("exception must be thrown");
                    }catch (NsiServiceException e){
                        Assert.assertEquals(NsiError.CONSTRAINT_VIOLATION, e.getError());
                        Assert.assertEquals( "Атрибут 'boolean_field' не прошел валидацию - значение 'test' не является логическим", e.getMessage());
                    }catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("incorrect exception throwed");
                    }
                    
                } finally {
                    platformSqlDao.dropSeq(dict, connection);
                }

            } finally {
                platformSqlDao.dropTable(dict, connection);
            }
        }
    }
    
    private DictRow createData(NsiQuery query, String key, String value) {
        DictRow inData = query.getDict().builder()
                .deleteMarkAttr(false)
                .idAttrNull()
                .lastChangeAttr(new DateTime().withMillisOfSecond(0))
                .lastUserAttr(null)
                // запишем во все атрибуты сначала коректные значения
                .attr("f1", "1")
                .attr("varchar_field", "qq")
                .attr("number_field_int", "23")
                .attr("number_field_double", "23.25")
                .attr("boolean_field", "false")
                .attr("datetime_field", "2015-11-13T08:55:22.861+02:00")
                .attr("char_field", "3")
                .attr("enum_field", "A")
                // и перезапишем значение атрибута для тестирования
                .attr(key, value)
                .attr("ORG_ID", 1L)
                .attr("ORG_ROLE_ID", 2L)
                .build();
        return inData;
    }
   
}
