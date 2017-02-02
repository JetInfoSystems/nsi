package jet.nsi.common.platform.oracle;

import java.math.BigDecimal;

import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultDataType;

import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlDao;
import jet.nsi.api.platform.PlatformSqlGen;

public class OracleNsiPlatform implements NsiPlatform {

    // jooq hack
    @SuppressWarnings("unused")
    private static DefaultDataType<String> VARCHAR2 = new DefaultDataType<String>(SQLDialect.DEFAULT,
            new DefaultDataType<String>(null, String.class, "varchar2"), "varchar2");
    @SuppressWarnings("unused")
    private static DefaultDataType<BigDecimal> NUMBER = new DefaultDataType<BigDecimal>(SQLDialect.DEFAULT,
            new DefaultDataType<BigDecimal>(null, BigDecimal.class, "number"), "number");

    //TODO: пока просто оставлю это здесь
    /*
    public static final DataType<Long>         BIGINT                   = new DefaultDataType<Long>(SQLDialect.DEFAULT, SQLDataType.BIGINT, "bigint");
    public static final DataType<Long>         INT8                     = new DefaultDataType<Long>(SQLDialect.DEFAULT, SQLDataType.BIGINT, "int8");
    public static final DataType<Double>       DOUBLEPRECISION          = new DefaultDataType<Double>(SQLDialect.DEFAULT, SQLDataType.DOUBLE, "double precision");
    public static final DataType<Double>       FLOAT8                   = new DefaultDataType<Double>(SQLDialect.DEFAULT, SQLDataType.FLOAT, "float8");
    public static final DataType<BigDecimal>   NUMERIC                  = new DefaultDataType<BigDecimal>(SQLDialect.DEFAULT, SQLDataType.NUMERIC, "numeric");
    public static final DataType<String>       VARCHAR                  = new DefaultDataType<String>(SQLDialect.DEFAULT, SQLDataType.VARCHAR, "varchar");
    public static final DataType<String>       CHAR                     = new DefaultDataType<String>(SQLDialect.DEFAULT, SQLDataType.CHAR, "char");
    public static final DataType<Date>         DATE                     = new DefaultDataType<Date>(SQLDialect.DEFAULT, SQLDataType.DATE, "date");
    */

    
    private static Settings settings = new Settings();
    static {
        settings.setRenderNameStyle(RenderNameStyle.AS_IS);
        //settings.setRenderNameStyle(RenderNameStyle.UPPER);
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
    public PlatformSqlGen getPlatformSqlGen() {
        return new OraclePlatformSqlGen(this);
    }

    @Override
    public PlatformSqlDao getPlatformSqlDao() {
        return new OraclePlatformSqlDao(this);
    }

    @Override
    public String getPlatformName() {
        return "oracle";
    }

}
