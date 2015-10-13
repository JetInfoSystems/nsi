package jet.isur.nsi.api.data;

import jet.isur.nsi.api.model.MetaOwn;


public class NsiConfigOwn {
    private String attr;

    public NsiConfigOwn(MetaOwn attr) {
        this.attr = attr.getAttr();
    }

    public String getAttr() {
        return attr;
    }
}
