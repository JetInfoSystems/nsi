package jet.isur.nsi.testkit.utils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.jooq.types.DayToSecond;
import org.jooq.types.YearToMonth;

public class OracleDataType {
    public static final DataType<Long>         BIGINT                   = new DefaultDataType<Long>(SQLDialect.DEFAULT, SQLDataType.BIGINT, "bigint");
    public static final DataType<Long>         INT8                     = new DefaultDataType<Long>(SQLDialect.DEFAULT, SQLDataType.BIGINT, "int8");
    public static final DataType<Double>       DOUBLEPRECISION          = new DefaultDataType<Double>(SQLDialect.DEFAULT, SQLDataType.DOUBLE, "double precision");
    public static final DataType<Double>       FLOAT8                   = new DefaultDataType<Double>(SQLDialect.DEFAULT, SQLDataType.FLOAT, "float8");
    public static final DataType<BigDecimal>   NUMERIC                  = new DefaultDataType<BigDecimal>(SQLDialect.DEFAULT, SQLDataType.NUMERIC, "numeric");
    public static final DataType<String>       VARCHAR                  = new DefaultDataType<String>(SQLDialect.DEFAULT, SQLDataType.VARCHAR, "varchar");
    public static final DataType<String>       CHAR                     = new DefaultDataType<String>(SQLDialect.DEFAULT, SQLDataType.CHAR, "char");
    public static final DataType<Date>         DATE                     = new DefaultDataType<Date>(SQLDialect.DEFAULT, SQLDataType.DATE, "date");

}
