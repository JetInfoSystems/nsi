package jet.nsi.common.platform.oracle;

import org.jooq.Condition;
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
    public void toSQL(RenderContext context) {
        
        context.sql("catsearch(").visit(field).sql(", ?, '') > 0");
    }
}
