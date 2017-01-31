package jet.nsi.api.platform;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jet.nsi.api.data.DictRow;
import org.jooq.DSLContext;
import org.jooq.DataType;

import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.MetaFieldType;

public interface PlatformSqlDao {

    void setClobParam(PreparedStatement ps, String value, int index) throws SQLException;

    void setBooleanParam(PreparedStatement ps, String value, int index) throws SQLException;

    long getCountFromRs(NsiQuery query, ResultSet rs) throws SQLException;
    
    String getFieldValue(ResultSet rs, int index, NsiConfigField field) throws SQLException;

    String getStringFromClob(ResultSet rs, int index) throws SQLException;

    String getClobStringValue(Clob clob) throws SQLException;

    void setParam(PreparedStatement ps, int index, MetaFieldType fieldType, int fieldSize, int fieldPrecision,
            Object value) throws SQLException;
    
    DataType<?> getDataType(MetaFieldType fieldType);

    DSLContext getQueryBuilder(Connection connection);

    void executeSql(Connection connection, String sql);

    int setParamsForUpdate(NsiQuery query, DictRow data, PreparedStatement ps) throws SQLException;

    String wrapFilterFieldValue(BoolExp filter, NsiConfigField field, String val);

    int limit(PreparedStatement ps, int index, long offset, int size) throws SQLException;

    void setParam(PreparedStatement ps, int index, NsiConfigField field,
                  String value) throws SQLException;

    void setParam(PreparedStatement ps, int index, MetaFieldType fieldType, int fieldSize,
                  String value) throws SQLException;

    String getFieldSpelling(String field);

    boolean useUUIDForId();
}
