package jet.isur.nsi.common.platform.client;

import org.jooq.SQLDialect;
import org.jooq.conf.Settings;

import jet.isur.nsi.api.sql.SqlDao;
import jet.isur.nsi.api.sql.SqlGen;

public interface NsiPlatformClient {
    SQLDialect getJooqSQLDialect();
    Settings getJooqSettings();
    SqlDao buildSqlDao(SqlGen sqlGen);
}
