package jet.isur.nsi.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dictGetReq")
public class DictGetReq extends BaseReq {

    private static final long serialVersionUID = 1L;
    private DictRowAttr id;
    public DictRowAttr getId() {
        return id;
    }
    public void setId(DictRowAttr id) {
        this.id = id;
    }

}
