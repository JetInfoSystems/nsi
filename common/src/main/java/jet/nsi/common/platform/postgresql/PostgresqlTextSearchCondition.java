package jet.nsi.common.platform.postgresql;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.RenderContext;
import org.jooq.impl.CustomCondition;

public class PostgresqlTextSearchCondition<T> extends CustomCondition implements Condition {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Field<T> field;
    
    public PostgresqlTextSearchCondition(Field<T> field) {
        this.field = field;
    }

    @Override
    public void toSQL(RenderContext context) {
        context.sql("to_tsvector(").visit(field).sql(") @@ to_tsquery(?)");
    }
}
