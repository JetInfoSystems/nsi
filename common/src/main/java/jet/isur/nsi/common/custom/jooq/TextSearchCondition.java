package jet.isur.nsi.common.custom.jooq;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.RenderContext;
import org.jooq.impl.CustomCondition;

public class TextSearchCondition<T> extends CustomCondition implements Condition {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Field<T> field;
    
    public TextSearchCondition(Field<T> field) {
        this.field = field;
    }
    
    @Override
    public void toSQL(RenderContext context) {
        
        context.sql("contains(").visit(field).sql(", ?, 1) > 0");
    }
}
