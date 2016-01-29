package jet.isur.nsi.migrator.platform.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.platform.PlatformSqlDao;
import jet.isur.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;

public class OracleFtsModule {

    private final PlatformSqlDao platformSqlDao;

    public OracleFtsModule(PlatformSqlDao platformSqlDao) {
        this.platformSqlDao = platformSqlDao;
    }
    
    public void updateFtsIndexesAfterPostproc(Connection connection, NsiConfigDict model) throws Exception {
        // получаем сведения о полнотекстовых индексах
        Map<String, String> databaseIndexes = getFtsDatabaseIndexes(connection, model);
        // формируем список требуемых полнотекстовых индексов
        Map<String, String> metadataIndexes = getFtsMetadataIndexes(connection, model);
        // проходим по списку имеющихся в базеданных полнотекстовых индексов на таблице
        for ( String column : databaseIndexes.keySet()) {
            // удаляем отсутствующие в метаданных
            if(!metadataIndexes.containsKey(column)) {
                dropFullSearchIndex(model, column, connection);
            }
        }
        // проходим по списку полей отмеченных для полнотекстового поиска
        for ( String column : metadataIndexes.keySet()) {
            // создаем индексы отсутствующие в бд 
            if(!databaseIndexes.containsKey(column)) {
                createFullSearchIndex(model, column, connection);
            }
        }
    }

    public Map<String, String> getFtsDatabaseIndexes(Connection connection,
            NsiConfigDict model) throws SQLException {
        Map<String, String> result = new HashMap<>();
        try(PreparedStatement ps = connection.prepareStatement(
                "select i.index_name, i.index_type, io.column_name "
                + "from user_indexes i "
                + "join user_ind_columns io on i.index_name=io.index_name "
                + "where i.table_name=Upper(?) and i.ITYP_OWNER='CTXSYS'")) {
            ps.setString(1, model.getTable());
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    String column = rs.getString(3);
                    result.put(column.toUpperCase(), name);
                }
            }
        }
        return result;
    }

    public Map<String, String> getFtsMetadataIndexes(Connection connection,
            NsiConfigDict model) throws SQLException {
        Map<String, String> result = new HashMap<>();
        for ( NsiConfigField field : model.getFields()) {
            if(field.isEnableFts()) {
                String name = NsiImplicitNamingStrategyImpl.compose("FTI", model.getTable(), field.getName(), 25);
                String column = field.getName();
                result.put(column.toUpperCase(), name);
            }
        }
        return result;
    }

    private String genFtsIndexName(NsiConfigDict dict, String field) {
        return NsiImplicitNamingStrategyImpl.compose("FTI", dict.getTable(), field, 25);
    }

    public void createFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("CREATE INDEX ")
                .append(genFtsIndexName(dict, field))
                .append(" ON ")
                .append(dict.getTable())
                .append("(").append(field).append(")")
                .append(" INDEXTYPE IS CTXSYS.CTXCAT ")
                .toString());
    }

    public void dropFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("DROP INDEX ").append(genFtsIndexName(dict, field)).toString());

    }

    public void recreateFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        try {
            createFullSearchIndex(dict, field, connection);
        }
        catch(Exception e) {
            dropFullSearchIndex(dict, field, connection);
            createFullSearchIndex(dict, field, connection);
        }
    }

}
