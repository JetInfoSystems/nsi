package jet.nsi.migrator.hibernate;

import org.hibernate.tool.schema.spi.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogActionsTargetImpl implements Target {
    private static final Logger log = LoggerFactory.getLogger(LogActionsTargetImpl.class);

    @Override
    public boolean acceptsImportScriptActions() {
        return true;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void accept(String action) {
        log.info( action );
    }

    @Override
    public void release() {
    }
}

