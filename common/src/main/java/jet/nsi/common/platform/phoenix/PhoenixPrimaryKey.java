package jet.nsi.common.platform.phoenix;

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;

import java.util.Iterator;

/**
 * Created by kazantsev on 13.01.17.
 */
public class PhoenixPrimaryKey extends PrimaryKey {
    public PhoenixPrimaryKey(Table table) {
        super(table);
    }

    @Override
    public String sqlConstraintString(Dialect dialect) {
        StringBuilder buf = new StringBuilder("constraint pk primary key (");
        Iterator iter = getColumnIterator();
        while (iter.hasNext()) {
            buf.append(((Column) iter.next()).getQuotedName(dialect));
            if (iter.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.append(')').toString();
    }
}
