package jet.isur.nsi.migrator.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.tool.schema.spi.Target;

public class RecActionsTargetImpl implements Target {

    private final List<String> actions;

    public RecActionsTargetImpl() {
        this.actions = new ArrayList<>();
    }

    @Override
    public boolean acceptsImportScriptActions() {
        return true;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void accept(String action) {
        actions.add( action );
    }

    @Override
    public void release() {
    }

    public List<String> getActions() {
        return actions;
    }
}

