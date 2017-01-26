package jet.nsi.common.platform.postgresql;

import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlDao;
import jet.nsi.api.platform.PlatformSqlGen;

public class PostgresqlNsiPlatform implements NsiPlatform {
    
    private static Settings settings = new Settings();
    static {
        settings.setRenderNameStyle(RenderNameStyle.AS_IS);
    }
    
    @Override
    public SQLDialect getJooqSQLDialect() {
        return SQLDialect.POSTGRES_9_5;
    }
    
    @Override
    public Settings getJooqSettings() {
        return settings;
    }
    
    @Override
    public PlatformSqlGen getPlatformSqlGen() {
        return new PostgresqlPlatformSqlGen(this);
    }
    
    @Override
    public PlatformSqlDao getPlatformSqlDao() {
        return new PostgresqlPlatformSqlDao(this);
    }

    @Override
    public String getPlatformName() {
        return "postgres";
    }
}
