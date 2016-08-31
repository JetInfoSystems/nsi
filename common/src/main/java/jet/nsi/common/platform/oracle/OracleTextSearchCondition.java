package jet.nsi.common.platform.oracle;

import org.jooq.BindContext;
import org.jooq.Condition;
import org.jooq.Context;
import org.jooq.Field;
import org.jooq.RenderContext;
import org.jooq.impl.CustomCondition;

public class OracleTextSearchCondition<T> extends CustomCondition implements Condition {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Field<T> field;
    
    public OracleTextSearchCondition(Field<T> field) {
        this.field = field;
    }

    @Override
    public void accept(Context<?> ctx) {
        if (ctx instanceof RenderContext) {
            ctx.sql("catsearch(").visit(field).sql(", ?, '') > 0");
        } else {
            bind((BindContext) ctx);
        }
        ctx.visit(this);
    }
}
