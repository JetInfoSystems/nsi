package jet.nsi.common.sql;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.QueryPart;
import org.jooq.RenderContext;
import org.jooq.SQLDialect;
import org.jooq.RenderContext.CastMode;
import org.jooq.conf.ParamType;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.NoConnectionProvider;

public class CustomizedDefaultDSLContext extends DefaultDSLContext {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    // -------------------------------------------------------------------------
    // XXX Constructors
    // -------------------------------------------------------------------------

    public CustomizedDefaultDSLContext(SQLDialect dialect) {
        super(dialect);
    }

    @SuppressWarnings("deprecation")
    public CustomizedDefaultDSLContext(SQLDialect dialect, Settings settings) {
        super(dialect, settings);
    }

    public CustomizedDefaultDSLContext(Connection connection, SQLDialect dialect) {
        super(connection, dialect);
    }

    @SuppressWarnings("deprecation")
    public CustomizedDefaultDSLContext(Connection connection, SQLDialect dialect, Settings settings) {
        super(connection, dialect, settings);
    }

    public CustomizedDefaultDSLContext(DataSource datasource, SQLDialect dialect) {
        super(datasource, dialect);
    }

    @SuppressWarnings("deprecation")
    public CustomizedDefaultDSLContext(DataSource datasource, SQLDialect dialect, Settings settings) {
        super(datasource, dialect, settings);
    }

    public CustomizedDefaultDSLContext(ConnectionProvider connectionProvider, SQLDialect dialect) {
        super(connectionProvider, dialect);
    }

    @SuppressWarnings("deprecation")
    public CustomizedDefaultDSLContext(ConnectionProvider connectionProvider, SQLDialect dialect, Settings settings) {
        super(connectionProvider, dialect, settings);
    }

    public CustomizedDefaultDSLContext(Configuration configuration) {
        super(configuration);
    }
    @Override
    public RenderContext renderContext() {
        return super.renderContext().castMode(CastMode.NEVER);
    }
 /*   
    
@Override
public String render(QueryPart part) {
    return renderContext().castMode(CastMode.NEVER).render(part);
}

@Override
public String renderNamedParams(QueryPart part) {
    return renderContext().castMode(CastMode.NEVER).paramType(ParamType.NAMED).render(part);
}

@Override
public String renderNamedOrInlinedParams(QueryPart part) {
    return renderContext().castMode(CastMode.NEVER).paramType(ParamType.NAMED_OR_INLINED).render(part);
}

@Override
public String renderInlined(QueryPart part) {
    return renderContext().castMode(CastMode.NEVER).paramType(ParamType.INLINED).render(part);
}*/
}
