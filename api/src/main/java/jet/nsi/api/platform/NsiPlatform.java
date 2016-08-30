package jet.nsi.api.platform;

import org.jooq.SQLDialect;
import org.jooq.conf.Settings;

public interface NsiPlatform {
    SQLDialect getJooqSQLDialect();
    Settings getJooqSettings();
    PlatformSqlGen getPlatformSqlGen();
    PlatformSqlDao getPlatformSqlDao();
}
