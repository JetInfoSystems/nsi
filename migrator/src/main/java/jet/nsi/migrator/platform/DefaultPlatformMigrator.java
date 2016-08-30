package jet.nsi.migrator.platform;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import org.jooq.DSLContext;

import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlDao;

public abstract class DefaultPlatformMigrator implements PlatformMigrator {

    private final NsiPlatform platform;
    protected final PlatformSqlDao platformSqlDao;
    
    public DefaultPlatformMigrator(NsiPlatform platform) {
        this.platform = platform;
        this.platformSqlDao = platform.getPlatformSqlDao();
    }
    
    @Override
    public NsiPlatform getPlatform() {
        return platform;
    }
    
    @Override
    public void removeUserProfile(Connection con, Long id) throws SQLException {
        DSLContext dsl = platformSqlDao.getQueryBuilder(con);
        dsl.delete(table("USER_PROFILE").as("u")).where(field("u.id").eq(id)).execute();
    }

    protected int countUserProfile(Connection con, String login) throws SQLException {
        DSLContext dsl = platformSqlDao.getQueryBuilder(con);
        return dsl.selectCount().from(table("USER_PROFILE").as("u"))
            .where(field("u.login").eq(login)).fetchOne(0, int.class);
    }

    @Override
    public Long createUserProfile(Connection con, String login) throws SQLException {
        int count = countUserProfile(con, login);

        if(count > 0) {
            return null;
        }

        DSLContext dsl = platformSqlDao.getQueryBuilder(con);
        Long id = dsl.nextval("SEQ_USER_PROFILE").longValue();
        dsl.insertInto(table("USER_PROFILE").as("u"),
            field("u.id"), field("u.IS_DELETED"), field("u.LOGIN"), field("u.STATE"))
            .values(id, "N", login, "1")
            .execute();

        return id;
    }

    @Override
    public void recreateTable(NsiConfigDict dict, Connection connection) {
        try {
            createTable(dict, connection);
        }
        catch(Exception e) {
            dropTable(dict, connection);
            createTable(dict, connection);
        }
    } 

    @Override
    public void recreateSeq(NsiConfigDict dict, Connection connection) {
        try {
            createSeq(dict, connection);
        }
        catch (Exception e) {
            dropSeq(dict,connection);
            createSeq(dict, connection);
        }
    }

    protected static void throwIfNot(SQLSyntaxErrorException e, int errorCode) {
        if(e.getErrorCode() != errorCode) {
            throw new RuntimeException(e);
        }
    }




}
