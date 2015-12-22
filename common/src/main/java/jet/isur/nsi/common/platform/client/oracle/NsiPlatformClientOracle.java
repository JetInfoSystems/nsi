package jet.isur.nsi.common.platform.client.oracle;

import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

import jet.isur.nsi.api.sql.SqlDao;
import jet.isur.nsi.api.sql.SqlGen;
import jet.isur.nsi.common.platform.client.NsiPlatformClient;

public class NsiPlatformClientOracle implements NsiPlatformClient {

    private static Settings settings = new Settings();
    static {
        settings.setRenderNameStyle(RenderNameStyle.AS_IS);
    }
    
    @Override
    public SQLDialect getJooqSQLDialect() {
        return SQLDialect.DEFAULT;
    }

    @Override
    public Settings getJooqSettings() {
        return settings;
    }

    @Override
    public SqlDao buildSqlDao(SqlGen sqlGen) {
        // TODO Auto-generated method stub
        return null;
    }

}
