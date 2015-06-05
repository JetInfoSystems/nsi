package jet.isur.nsi.api.data;

import jet.isur.nsi.api.model.BoolExp;

public class BoolExpVisitor {

    public void accept(BoolExp filter) {
        visit(filter);
        if(filter.getExpList() != null) {
            for ( BoolExp exp : filter.getExpList()) {
                accept(exp);
            }
        }
    }

    protected void visit(BoolExp filter) {
    }
}
