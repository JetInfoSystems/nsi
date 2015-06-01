package jet.isur.nsi.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dictCountReq")
public class DictCountReq extends BaseReq {

    private static final long serialVersionUID = 1L;
    private DictRowAttr parent;
    private DictRowAttr owner;
    private BoolExp filter;
    public DictRowAttr getParent() {
        return parent;
    }
    public void setParent(DictRowAttr parent) {
        this.parent = parent;
    }
    public DictRowAttr getOwner() {
        return owner;
    }
    public void setOwner(DictRowAttr owner) {
        this.owner = owner;
    }
    public BoolExp getFilter() {
        return filter;
    }
    public void setFilter(BoolExp filter) {
        this.filter = filter;
    }

}
