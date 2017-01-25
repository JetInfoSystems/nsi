package jet.nsi.common.platform.phoenix;

import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlDao;
import jet.nsi.api.platform.PlatformSqlGen;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

public class PhoenixNsiPlatform implements NsiPlatform {
    
    private static Settings settings = new Settings();
    static {

        settings.setRenderNameStyle(RenderNameStyle.AS_IS);
    }
    
    @Override
    public SQLDialect getJooqSQLDialect() {
        return SQLDialect.DEFAULT;
    } //todo
    
    @Override
    public Settings getJooqSettings() {
        return settings;
    }
    
    @Override
    public PlatformSqlGen getPlatformSqlGen() {
        return new PhoenixPlatformSqlGen(this);
    }
    
    @Override
    public PlatformSqlDao getPlatformSqlDao() {
        return new PhoenixPlatformSqlDao(this);
    }

    @Override
    public String getPlatformName() {
        return "PHOENIX";
    }
}
