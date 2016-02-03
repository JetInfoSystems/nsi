package jet.isur.nsi.services.platform.oracle;

import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.MetaField;
import jet.isur.nsi.api.model.OperationType;
import jet.isur.nsi.common.platform.oracle.OracleNsiPlatform;
import jet.isur.nsi.common.platform.oracle.OraclePlatformSqlDao;
import org.junit.Assert;
import org.junit.Test;

public class OraclePlatformSqlDaoTest {

    @Test
    public void filterOnDeleteAllIllegalCharactersTest() {
        BoolExp filter = new BoolExp();
        filter.setFunc(OperationType.CONTAINS);
        String testValue = "\"),(";
        String secondTestValue = "ОАО \"Нефть\" (газ, масло)";
        NsiConfigField field = new NsiConfigField(new MetaField());
        OraclePlatformSqlDao sqlDao = new OraclePlatformSqlDao(new OracleNsiPlatform());

        String firstActualParam = sqlDao.wrapFilterFieldValue(filter, field, testValue);
        String secondActualParam = sqlDao.wrapFilterFieldValue(filter, field, secondTestValue);
        System.out.println(firstActualParam + secondActualParam);
        Assert.assertEquals("***", firstActualParam);
        Assert.assertEquals("**ОАО Нефть газ масло*", secondActualParam);
    }

}
