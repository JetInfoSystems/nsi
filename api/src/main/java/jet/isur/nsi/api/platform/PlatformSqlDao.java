package jet.isur.nsi.api.platform;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.DataType;

import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.MetaFieldType;

public interface PlatformSqlDao {

    long getCountFromRs(NsiQuery query, ResultSet rs) throws SQLException;
    
    String getFieldValue(ResultSet rs, int index, NsiConfigField field) throws SQLException;

    String getClobStringValue(Clob clob) throws SQLException;

    void setParam(PreparedStatement ps, int index, MetaFieldType fieldType, int fieldSize, int fieldPrecision,
            Object value) throws SQLException;
    
    DataType<?> getDataType(MetaFieldType fieldType);

    DSLContext getQueryBuilder(Connection connection);

    void executeSql(Connection connection, String sql);

    String wrapFilterFieldValue(BoolExp filter, NsiConfigField field, String val);
    
}
